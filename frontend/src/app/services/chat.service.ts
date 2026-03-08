import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private readonly apiUrl = 'http://localhost:8080/api/chat';

  constructor(private http: HttpClient) {}

  send(message: string): Observable<string> {
    return this.http.post(this.apiUrl, message, {
      headers: new HttpHeaders({ 'Content-Type': 'text/plain' }),
      responseType: 'text'
    });
  }

  sendStream(message: string): Observable<string> {
    return this.http.post(this.apiUrl, message, {
      headers: new HttpHeaders({ 'Content-Type': 'text/plain' }),
      responseType: 'text'
    });
  }
}
