import {ChangeDetectorRef, NgZone, Component, OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDividerModule } from '@angular/material/divider';
import {Category, DataService, DocumentDto} from '../DataService/DataService';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';

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
    MatSelectModule
  ]
})
export class ButtonOverviewExample implements OnInit{
  documents: DocumentDto[] = [];
  displayedColumns = ['id', 'title', 'categories', 'contentType', 'fileSize', 'uploadedAt', 'actions'];

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

  searchTerm: string = '';
  availableCategories: Category[] = [];
  uploadCategory = '';

  readonly MAX_SIZE_MB = 10;

  constructor(private data: DataService, private cdr: ChangeDetectorRef, private zone: NgZone) {}

  // Load categories when component starts
  ngOnInit() {
    this.loadCategories();
    this.onGet(); // (Assuming you want to load docs on start too)
  }

  loadCategories() {
    this.data.getCategories().subscribe({
      next: (cats) => this.availableCategories = cats,
      error: (err) => console.error('Failed to load categories', err)
    });
  }

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

    if (!file) {
      this.selectedFile = undefined;
      return;
    }

    // Check File Type
    if (file.type !== 'application/pdf') {
      alert('Only PDF files are allowed.');
      this.selectedFile = undefined;
      // Reset the input so they can try again
      (event.target as HTMLInputElement).value = '';
      return;
    }

    // Check File Size (MB * 1024 * 1024 = Bytes)
    const maxBytes = this.MAX_SIZE_MB * 1024 * 1024;
    if (file.size > maxBytes) {
      alert(`File is too big! Max size is ${this.MAX_SIZE_MB}MB.`);
      this.selectedFile = undefined;
      (event.target as HTMLInputElement).value = '';
      return;
    }

    this.selectedFile = file;
  }

  onUpload() {
    if (!this.selectedFile) { alert('no file selected!'); return; }

    this.uploading = true;
    const formData = new FormData();
    formData.append('file', this.selectedFile);
    formData.append('title', this.uploadTitle || this.selectedFile.name);

    if (this.uploadCategory) {
      formData.append('category', this.uploadCategory);
    }
    fetch('http://localhost:8080/api/documents/upload', { method: 'POST', body: formData })
      .then(async res => {
        this.onGet(); // lädt Liste & startet ggf. OCR-Polling
        const text = await res.text();
        this.uploadResult = res.ok ? 'upload successful: ' + text : 'Error: ' + text;
        this.loadCategories();
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

// ----- Search -----
  onSearch() {
    if (!this.searchTerm.trim()) {
      // Use 'this.data' (not dataService) and add type '(docs: any)'
      this.data.getDocuments().subscribe((docs: any) => this.documents = docs);
      return;
    }

    // Use 'this.data' here too
    this.data.searchDocuments(this.searchTerm).subscribe({
      next: (results: any) => {
        this.documents = results;
      },
      error: (err: any) => {
        console.error('Search failed:', err);
      }
    });
  }

  //=================================================================
  //=========================== Top secret ==========================
  //=================================================================

  readonly gifPool = [
    'https://media0.giphy.com/media/v1.Y2lkPTc5MGI3NjExYXFpem01ZjFkZTNoajFhOGZkdzh5emUwaTVnOHM4cjM2OXp5c20zOCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/TcdpZwYDPlWXC/giphy.gif',
    'https://media1.giphy.com/media/v1.Y2lkPTc5MGI3NjExY2hxdHE4MjYxZjJ4dmp3MW05bWllcmNieWVpbWQ4a2FuN3czeHMwbyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/3BKf0I2PVxAfC/giphy.gif',
    'https://media0.giphy.com/media/v1.Y2lkPTc5MGI3NjExbDliaHJ2OTFvNnI0a2Y1ZHdqZmJyeGE5bndqeXg2M2lzY3R5YWx3eCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/mFdnWF1RTI7fi/giphy.gif',
    'https://media.giphy.com/media/v1.Y2lkPWVjZjA1ZTQ3b2kxOWswd2ZoNmIxcm54MjB2bnN1bHczZDc3dm4zNm51ZHhuMHF4YSZlcD12MV9naWZzX3JlbGF0ZWQmY3Q9Zw/4KLv24CPUoZ0I/giphy.gif',
    'https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExaTFneWFzbmQ0MXJjNWM5end3dTY4dmdhZHJocTZjNWpobHdsZDF2cCZlcD12MV9naWZzX3NlYXJjaCZjdD1n/QxcSqRe0nllClKLMDn/giphy.gif',
    'https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExYmtjeXZjdmpzdjBxczE1OHJ0ajM4dGE3bXN1OHR2MjV2aXhlODA2cyZlcD12MV9naWZzX3NlYXJjaCZjdD1n/nbKKMfmeDknzq/giphy.gif'
  ];

  private rowGifMap = new Map<number, string>();

  getGif(id: number): string {
    if (!this.rowGifMap.has(id)) {
      const randomGif = this.gifPool[Math.floor(Math.random() * this.gifPool.length)];
      this.rowGifMap.set(id, randomGif);
    }
    return `url('${this.rowGifMap.get(id)}')`;
  }
}
