import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Pipe({ name: 'markdown', standalone: true })
export class MarkdownPipe implements PipeTransform {
  constructor(private sanitizer: DomSanitizer) {}

  transform(value: string): SafeHtml {
    if (!value) return '';
    return this.sanitizer.bypassSecurityTrustHtml(markdownToHtml(value));
  }
}

function markdownToHtml(md: string): string {
  const lines = md.split('\n');
  const out: string[] = [];
  let inList = false;
  let i = 0;

  const closeList = () => {
    if (inList) { out.push('</ul>'); inList = false; }
  };

  while (i < lines.length) {
    const line = lines[i];
    const trimmed = line.trim();

    // Detect markdown table: current line is a row, next line is a separator |---|
    if (/^\|.+\|/.test(trimmed) && i + 1 < lines.length && /^\|[\s\-:|]+\|/.test(lines[i + 1].trim())) {
      closeList();
      const headers = splitRow(line);
      i += 2; // skip header + separator row
      const bodyRows: string[][] = [];
      while (i < lines.length && /^\|.+\|/.test(lines[i].trim())) {
        bodyRows.push(splitRow(lines[i]));
        i++;
      }
      out.push('<div class="md-table-wrap"><table class="md-table">');
      out.push('<thead><tr>' + headers.map(c => `<th>${inline(c)}</th>`).join('') + '</tr></thead>');
      out.push('<tbody>');
      bodyRows.forEach(row => {
        out.push('<tr>' + row.map(c => `<td>${inline(c)}</td>`).join('') + '</tr>');
      });
      out.push('</tbody></table></div>');
      continue;
    }

    // Headings (###, ##, #)
    const h = trimmed.match(/^(#{1,6})\s+(.+)/);
    if (h) {
      closeList();
      const level = Math.min(h[1].length, 4);
      out.push(`<h${level}>${inline(h[2])}</h${level}>`);
    }
    // Horizontal rule
    else if (/^---+$/.test(trimmed)) {
      closeList();
      out.push('<hr>');
    }
    // List item (- or *)
    else if (/^[*-] .+/.test(trimmed)) {
      if (!inList) { out.push('<ul>'); inList = true; }
      out.push(`<li>${inline(trimmed.slice(2))}</li>`);
    }
    // Blank line
    else if (!trimmed) {
      closeList();
    }
    // Regular paragraph
    else {
      closeList();
      out.push(`<p>${inline(line)}</p>`);
    }

    i++;
  }

  closeList();
  return out.join('');
}

function splitRow(line: string): string[] {
  const s = line.trim().replace(/^\|/, '').replace(/\|$/, '');
  return s.split('|').map(c => c.trim());
}

function inline(text: string): string {
  return text
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/(?<!\*)\*([^*]+)\*(?!\*)/g, '<em>$1</em>')
    .replace(/`([^`]+)`/g, '<code>$1</code>');
}
