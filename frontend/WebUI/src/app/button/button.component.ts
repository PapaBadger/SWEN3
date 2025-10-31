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
import {MatTableModule} from '@angular/material/table';

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
    MatTableModule
    ]
})
export class ButtonOverviewExample {
  /** Current list shown in the UI (filled by GET). */
  documents: DocumentDto[] = [];
  id?: number;

  /** Form state for PUT (update). */
  updateId?: number;
  updateTitle = '';
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

  /**
   * Update an existing document (PUT).
   * Requires an ID; applies new title/content if provided and refreshes the list.
   */
  async onUpdate() {
    if (!this.updateId || this.updateId <= 0) return;
    this.updating = true;
    try {
      const updated = await this.data.updateDocument(this.updateId, {
        title: this.updateTitle
      });
      console.log('Updated:', updated);
      this.updateTitle = '';
      this.updateId = undefined;
      this.onGet();
    } catch (e) {
      console.error('PUT failed:', e);
    } finally {
      this.updating = false;
    }
  }

  selectedFile?: File;
  uploadTitle: string = '';
  uploading = false;
  uploadResult?: string;

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file && file.type === 'application/pdf') {
      this.selectedFile = file;
    } else {
      alert('only PDF files.');
      this.selectedFile = undefined;
    }
  }

  onUpload() {
    if (!this.selectedFile) {
      alert('no file selected!');
      return;
    }

    this.uploading = true;
    const formData = new FormData();
    formData.append('file', this.selectedFile);
    formData.append('title', this.uploadTitle || this.selectedFile.name);

    fetch('http://localhost:8080/api/documents/upload', {
      method: 'POST',
      body: formData
    })
      .then(async res => {
        this.onGet()
        const text = await res.text();
        this.uploadResult = res.ok
          ? 'upload successful: ' + text
          : 'Error: ' + text;
      })
      .catch(err => {
        this.uploadResult = 'upload failed: ' + err.message;
      })
      .finally(() => (this.uploading = false));
  }

  onDownload(id: number) {
    this.data.downloadDocument(id)
      .then(blob => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `document-${id}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      })
      .catch(err => console.error('Download failed', err));
  }


  /**
   * Delete a document by ID (DELETE).
   * Clears the input and refreshes the list on success.
   */
  onDeleteByID(id: number) {
    this.deleting = true;
    this.data.deleteDocument(id)
      .then(() => this.onGet()) // refresh list
      .finally(() => this.deleting = false);
  }
}
