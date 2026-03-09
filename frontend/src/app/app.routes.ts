import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'chat', pathMatch: 'full' },
  {
    path: 'chat',
    loadComponent: () => import('./pages/chat/chat.component').then(m => m.ChatComponent)
  },
  {
    path: 'models',
    loadComponent: () => import('./pages/ai-models/ai-models.component').then(m => m.AiModelsComponent)
  }
];
