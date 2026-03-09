import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AiModel } from '../models/ai-model.model';

@Injectable({ providedIn: 'root' })
export class AiModelService {
  private readonly base = 'http://localhost:8080/api/ai-models';

  constructor(private http: HttpClient) {}

  findAll(): Observable<AiModel[]> {
    return this.http.get<AiModel[]>(this.base);
  }

  toggle(id: number): Observable<AiModel> {
    return this.http.patch<AiModel>(`${this.base}/${id}/toggle`, null);
  }

  update(id: number, model: AiModel): Observable<AiModel> {
    return this.http.put<AiModel>(`${this.base}/${id}`, model);
  }
}
