import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export interface ToastMessage {
  type: 'success' | 'error' | 'info';
  text: string;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  readonly messages$ = new Subject<ToastMessage>();

  success(text: string): void {
    this.messages$.next({ type: 'success', text });
  }

  error(text: string): void {
    this.messages$.next({ type: 'error', text });
  }

  info(text: string): void {
    this.messages$.next({ type: 'info', text });
  }
}
