import {Component} from '@angular/core';
import {MatIconModule} from '@angular/material/icon';
import { CommonModule } from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {DataService, DocumentDto} from '../DataService/DataService';
import {MatInputModule} from '@angular/material/input';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatDividerModule} from '@angular/material/divider';
import {MatList} from '@angular/material/list';
import {MatListItem} from '@angular/material/list';
import {FormsModule} from '@angular/forms';

/**
 * ButtonOverviewExample (standalone component)
 * -------------------------------------------
 * Small demo component that wires UI buttons/inputs to the DataService,
 * proving end-to-end CRUD from Angular → Spring Boot → Postgres.
 *
 * Responsibilities:
 * - Present simple inputs for create/update/delete.
 * - Call DataService methods on clicks.
 * - Display the current list of documents.
 *
 * Why standalone?
 * - Simpler to embed and test (no NgModule needed).
 */
@Component({
  selector: 'button-overview-example',
  templateUrl: 'button-component.html',
  styleUrl: 'button-component.css',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatDividerModule,
    MatList,
    MatListItem]
})
export class ButtonOverviewExample {
  /** Current list shown in the UI (filled by GET). */
  documents: DocumentDto[] = [];

  /** Form state for POST (create). */
  newTitle = '';
  newContent = '';
  creating = false;

  /** Form state for PUT (update). */
  updateId?: number;
  updateTitle = '';
  updateContent = '';
  updating = false;

  /** Form state for DELETE. */
  deleteId?: number;
  deleting = false;

  constructor(private data: DataService) {}

  /**
   * Load all documents (GET).
   * Subscribes to the DataService observable and updates the local list.
   */
  onGet() {
    this.data.getDocuments().subscribe({
      next: docs => {
        this.documents = docs;
        console.log('Documents:', docs);
      },
      error: err => console.error('GET failed:', err)
    });
  }

  /**
   * Create a new document (POST).
   * Uses async/await over a Promise for readability; refreshes the list on success.
   */
  async onCreate() {
    if (!this.newTitle.trim()) return;
    this.creating = true;
    try {
      const created = await this.data.createDocument({ title: this.newTitle, content: this.newContent });
      console.log('Created:', created);
      this.newTitle = '';
      this.newContent = '';
      this.onGet(); // refresh list
    } catch (e) {
      console.error('POST failed:', e);
    } finally {
      this.creating = false;
    }
  }

  /**
   * Update an existing document (PUT).
   * Requires an ID; applies new title/content if provided and refreshes the list.
   */
  async onUpdate() {
    if (!this.updateId || this.updateId <= 0) return;
    this.updating = true;
    try {
      const updated = await this.data.updateDocument(this.updateId, {
        title: this.updateTitle,
        content: this.updateContent
      });
      console.log('Updated:', updated);
      this.updateTitle = '';
      this.updateContent = '';
      this.updateId = undefined;
      this.onGet();
    } catch (e) {
      console.error('PUT failed:', e);
    } finally {
      this.updating = false;
    }
  }

  /**
   * Delete a document by ID (DELETE).
   * Clears the input and refreshes the list on success.
   */
  async onDelete() {
    if (!this.deleteId || this.deleteId <= 0) return;
    this.deleting = true;
    try {
      await this.data.deleteDocument(this.deleteId);
      console.log('Deleted', this.deleteId);
      this.deleteId = undefined;
      this.onGet();
    } catch (e) {
      console.error('DELETE failed:', e);
    } finally {
      this.deleting = false;
    }
  }
}
