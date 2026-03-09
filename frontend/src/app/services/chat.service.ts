import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ChatService {
  private readonly apiUrl = 'http://localhost:8080/api/chat';

  constructor(private http: HttpClient) {}

  send(message: string, modelId?: number | null): Observable<string> {
    return this.http.post(this.apiUrl, { message, modelId: modelId ?? null }, {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
      responseType: 'text'
    });
  }

  sendStream(message: string, modelId?: number | null): Observable<string> {
    return this.http.post(`${this.apiUrl}/stream`, { message, modelId: modelId ?? null }, {
      headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
      responseType: 'text'
    });
  }
}
