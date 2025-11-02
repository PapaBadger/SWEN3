import {ChangeDetectorRef, NgZone, Component} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDividerModule } from '@angular/material/divider';
import { DataService, DocumentDto } from '../DataService/DataService';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';

@Component({
  selector: 'button-overview-example',
  templateUrl: 'button-component.html',
  styleUrl: 'button-component.css',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatDividerModule,
    MatProgressSpinnerModule,
  ]
})
export class ButtonOverviewExample {
  documents: DocumentDto[] = [];
  displayedColumns = ['id','title','contentType','fileSize','uploadedAt','actions'];

  expandedId: number | null = null;
  ocrState = new Map<number, { loading: boolean; text?: string; error?: string }>();

  // --- Form-States ---
  updateId?: number;
  updateTitle = '';
  updating = false;

  deleteId?: number;
  deleting = false;

  selectedFile?: File;
  uploadTitle = '';
  uploading = false;
  uploadResult?: string;

  constructor(private data: DataService, private cdr: ChangeDetectorRef, private zone: NgZone) {}

  private setOcrState(id: number, state: { loading: boolean; text?: string; error?: string }) {
    const next = new Map(this.ocrState);
    next.set(id, state);

    this.zone.run(() => {
      this.ocrState = next;
      this.cdr.detectChanges();
    });
  }

  onGet() {
    this.data.getDocuments().subscribe({
      next: docs => {
        this.documents = docs;
        docs.filter(d => !!d.id && !d.ocrText).forEach(d => this.startOcrPolling(d.id));
      },
      error: err => console.error('GET failed:', err)
    });
  }

  toggleOcr(row: DocumentDto) {
    if (!row.id) return;
    if (this.expandedId === row.id) { this.expandedId = null; return; }
    this.expandedId = row.id;

    const state = this.ocrState.get(row.id);
    if (state?.text || state?.loading) return;

    if (row.ocrText) {
      this.setOcrState(row.id, { loading: false, text: row.ocrText });
      return;
    }
    this.loadOcrOnce(row.id);
  }

  private loadOcrOnce(id: number) {
    this.setOcrState(id, { loading: true });
    this.data.getSummaryText(id).subscribe({
      next: txt => this.setOcrState(id, { loading: false, text: txt }),
      error: _ => this.setOcrState(id, { loading: false, error: 'OCR-Text konnte nicht geladen werden' })
    });
  }

  private startOcrPolling(id: number) {
    const current = this.ocrState.get(id);
    if (current?.text || current?.loading) return;

    let tries = 0;
    const poll = () => {
      this.setOcrState(id, { loading: true });
      this.data.getSummaryText(id).subscribe({
        next: txt => {
          if (txt && txt.trim().length > 0) {
            this.setOcrState(id, { loading: false, text: txt });
          } else {
            tries++;
            const delay = Math.min(1500 * Math.pow(1.25, tries), 5000);
            setTimeout(poll, delay);
          }
        },
        error: _ => {
          tries++;
          const delay = Math.min(1500 * Math.pow(1.25, tries), 5000);
          setTimeout(poll, delay);
        }
      });
    };
    poll();
  }

  // ----- Update -----
  async onUpdate() {
    if (!this.updateId || this.updateId <= 0) return;
    this.updating = true;
    try {
      const updated = await this.data.updateDocument(this.updateId, { title: this.updateTitle });
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

  // ----- Upload -----
  onFileSelected(event: Event) {
    const file = (event.target as HTMLInputElement).files?.[0] ?? null;
    if (file && file.type === 'application/pdf') this.selectedFile = file;
    else { alert('only PDF files.'); this.selectedFile = undefined; }
  }

  onUpload() {
    if (!this.selectedFile) { alert('no file selected!'); return; }

    this.uploading = true;
    const formData = new FormData();
    formData.append('file', this.selectedFile);
    formData.append('title', this.uploadTitle || this.selectedFile.name);

    fetch('http://localhost:8080/api/documents/upload', { method: 'POST', body: formData })
      .then(async res => {
        this.onGet(); // lädt Liste & startet ggf. OCR-Polling
        const text = await res.text();
        this.uploadResult = res.ok ? 'upload successful: ' + text : 'Error: ' + text;
      })
      .catch(err => this.uploadResult = 'upload failed: ' + err.message)
      .finally(() => this.uploading = false);
  }

  // ----- Download -----
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

  // ----- Delete -----
  onDeleteByID(id: number) {
    this.deleting = true;
    this.data.deleteDocument(id)
      .then(() => this.onGet())
      .finally(() => this.deleting = false);
  }
}
