import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PoetryService } from './poetry.service';

export interface Poem {
  title: string;
  author: string;
  lines: string[];
  linecount: string;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  providers: [PoetryService]
})
export class AppComponent {
  title = 'Poetry Explorer';
  poems: Poem[] = [];
  searchQuery = '';
  searchType: 'author' | 'title' = 'author';
  loading = false;
  error = '';

  constructor(private poetryService: PoetryService) {}

  /**
   * Search for poems based on the selected search type and query
   */
  searchPoems(): void {
    if (!this.searchQuery.trim()) {
      this.error = 'Please enter a search term';
      return;
    }

    this.loading = true;
    this.error = '';
    this.poems = [];

    this.poetryService.searchPoems(this.searchType, this.searchQuery).subscribe({
      next: (data: Poem[]) => {
        this.poems = data;
        this.loading = false;
        if (!data || data.length === 0) {
          this.error = 'No poems found. Try a different search!';
        }
      },
      error: (err: Error) => {
        this.error = err.message || 'Failed to fetch poems. Please try again.';
        this.loading = false;
      }
    });
  }

  /**
   * Get a random poem from the database
   */
  getRandomPoem(): void {
    this.loading = true;
    this.error = '';
    this.poems = [];
    this.searchQuery = '';

    this.poetryService.getRandomPoems(1).subscribe({
      next: (data: Poem[]) => {
        this.poems = data;
        this.loading = false;
        if (!data || data.length === 0) {
          this.error = 'Could not fetch a random poem. Please try again.';
        }
      },
      error: (err: Error) => {
        this.error = err.message || 'Failed to fetch a random poem. Please try again.';
        this.loading = false;
      }
    });
  }

  /**
   * Clear the search and reset the view
   */
  clearSearch(): void {
    this.searchQuery = '';
    this.poems = [];
    this.error = '';
  }
}
