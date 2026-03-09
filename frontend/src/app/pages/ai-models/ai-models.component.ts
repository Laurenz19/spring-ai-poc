import { Component, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgClass } from '@angular/common';
import { AiModelService } from '../../services/ai-model.service';
import { AiModel, Provider } from '../../models/ai-model.model';

@Component({
  selector: 'app-ai-models',
  imports: [FormsModule, NgClass],
  templateUrl: './ai-models.component.html',
  styleUrl: './ai-models.component.scss'
})
export class AiModelsComponent implements OnInit {
  models = signal<AiModel[]>([]);
  editingId = signal<number | null>(null);
  editForm: Partial<AiModel> = {};

  readonly providerMeta: Record<Provider, { label: string; icon: string; color: string }> = {
    GROQ:      { label: 'Groq',      icon: 'bolt',          color: 'orange'  },
    GEMINI:    { label: 'Gemini',    icon: 'auto_awesome',  color: 'blue'    },
    OLLAMA:    { label: 'Ollama',    icon: 'computer',      color: 'green'   },
    ANTHROPIC: { label: 'Anthropic', icon: 'psychology',    color: 'purple'  },
    OPENAI:    { label: 'OpenAI',    icon: 'smart_toy',     color: 'teal'    },
  };

  constructor(private svc: AiModelService) {}

  ngOnInit() { this.load(); }

  load() {
    this.svc.findAll().subscribe(data => this.models.set(data));
  }

  toggle(model: AiModel) {
    this.svc.toggle(model.id).subscribe(updated =>
      this.models.update(list => list.map(m => m.id === updated.id ? updated : m))
    );
  }

  startEdit(model: AiModel) {
    this.editingId.set(model.id);
    this.editForm = { ...model };
  }

  cancelEdit() {
    this.editingId.set(null);
    this.editForm = {};
  }

  saveEdit() {
    const id = this.editingId();
    if (id == null) return;
    const model = this.models().find(m => m.id === id)!;
    const updated: AiModel = { ...model, ...this.editForm };
    this.svc.update(id, updated).subscribe(saved => {
      this.models.update(list => list.map(m => m.id === saved.id ? saved : m));
      this.cancelEdit();
    });
  }

  maskKey(key: string | null): string {
    if (!key || key.length < 8) return key ? '••••••••' : '—';
    return key.slice(0, 4) + '••••••••' + key.slice(-4);
  }
}
