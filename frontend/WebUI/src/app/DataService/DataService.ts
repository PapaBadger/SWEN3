import {Component, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';

/**
 * @title Button overview
 */
@Injectable({ providedIn: 'root' })
export class DataService {
  constructor(private http: HttpClient) {
  }

  getDocuments() {
    return this.http.get('api/documents');
  }

  //uploadDocuments() {
    //return this.http.post('api/documents')
  //}
}
