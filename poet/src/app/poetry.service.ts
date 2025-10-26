import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

export interface Poem {
  title: string;
  author: string;
  lines: string[];
  linecount: string;
}

@Injectable({
  providedIn: 'root'
})
export class PoetryService {
  private baseUrl = 'https://poetrydb.org';

  constructor(private http: HttpClient) {}

  /**
   * Search for poems by author or title
   * @param searchType - Either 'author' or 'title'
   * @param query - The search query
   * @returns Observable of poem array
   */
  searchPoems(searchType: 'author' | 'title', query: string): Observable<Poem[]> {
    const url = `${this.baseUrl}/${searchType}/${encodeURIComponent(query)}`;
    
    return this.http.get<Poem[]>(url).pipe(
      map(response => {
        // Handle the case where API returns an object with status property instead of poems
        if (response && typeof response === 'object' && 'status' in response) {
          return [];
        }
        return response;
      }),
      catchError(this.handleError)
    );
  }

  /**
   * Search for poems by both author and title
   * @param author - Author name
   * @param title - Poem title
   * @returns Observable of poem array
   */
  searchByAuthorAndTitle(author: string, title: string): Observable<Poem[]> {
    const url = `${this.baseUrl}/author,title/${encodeURIComponent(author)};${encodeURIComponent(title)}`;
    
    return this.http.get<Poem[]>(url).pipe(
      map(response => {
        if (response && typeof response === 'object' && 'status' in response) {
          return [];
        }
        return response;
      }),
      catchError(this.handleError)
    );
  }

  /**
   * Get random poems
   * @param count - Number of random poems to fetch
   * @returns Observable of poem array
   */
  getRandomPoems(count: number = 1): Observable<Poem[]> {
    const url = `${this.baseUrl}/random/${count}`;
    
    return this.http.get<Poem[]>(url).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Handle HTTP errors
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'An error occurred while fetching poems.';
    
    // Check for non-200 status codes
    if (error.status !== 200 && error.status !== 0) {
      if (error.status === 404) {
        errorMessage = 'No poems found matching your search. Please try a different query.';
      } else {
        errorMessage = `Server returned status ${error.status}: ${error.statusText || 'Unknown error'}`;
      }
    } else if (error.error instanceof ErrorEvent) {
      // Client-side or network error
      errorMessage = `Network error: ${error.error.message}`;
    } else if (error.status === 0) {
      errorMessage = 'Unable to connect to the server. Please check your internet connection.';
    }
    
    console.error('Poetry API Error:', error);
    return throwError(() => new Error(errorMessage));
  }
}
