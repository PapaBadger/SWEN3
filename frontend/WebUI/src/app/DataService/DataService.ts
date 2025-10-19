import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom, Observable } from 'rxjs';

/**
 * DTO (Data Transfer Object) representing a Document as exposed by the backend API.
 * Mirrors the Spring Boot `Document` entity fields used in the REST endpoints.
 */

export interface DocumentDto {
  id?: number;
  title: string;
}

/**
 * DataService
 * ------------
 * Thin HTTP layer that talks to the Spring Boot backend.
 * Keeps all REST calls in one place so components stay clean and testable.
 *
 * Responsibilities:
 * - Provide typed methods for CRUD operations on documents.
 * - Hide raw HttpClient details from the component.
 *
 * Note: The base URL points to your backend. From a browser, that is localhost:8080.
 *       You can later move this into Angular environments if needed.
 */
@Injectable({ providedIn: 'root' })
export class DataService {
  private readonly baseUrl = 'http://localhost:8080/api/documents';

  constructor(private http: HttpClient) {}

  /**
   * GET /api/documents
   * Fetch all documents from the backend as an observable stream.
   */
  getDocuments(): Observable<DocumentDto[]> {
    return this.http.get<DocumentDto[]>(this.baseUrl);
  }
  /**
   * PUT /api/documents/{id}
   * Update an existing document by id. Returns the updated entity.
   */
  updateDocument(id: number, doc: DocumentDto): Promise<DocumentDto> {
    return firstValueFrom(this.http.put<DocumentDto>(`${this.baseUrl}/${id}`, doc));
  }

  //download doc
  downloadDocument(id: number): Promise<Blob> {
    return firstValueFrom(
      this.http.get(`${this.baseUrl}/${id}/download`, {
        responseType: 'blob'
      })
    );
  }


  /**
   * DELETE /api/documents/{id}
   * Remove a document by id.
   */
  deleteDocument(id: number): Promise<void> {
    return firstValueFrom(this.http.delete<void>(`${this.baseUrl}/${id}`));
  }
}
