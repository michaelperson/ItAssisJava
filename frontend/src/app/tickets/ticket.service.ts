import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Escalade, Ticket } from './modele';

/**
 * Accès à l'API tickets. Les composants n'appellent jamais HttpClient
 * directement : toute route de l'API est exposée par un service dédié.
 */
@Injectable({ providedIn: 'root' })
export class TicketService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/tickets';

  lister(service?: string, categorie?: string): Observable<readonly Ticket[]> {
    let params = new HttpParams();
    if (service) {
      params = params.set('service', service);
    }
    if (categorie) {
      params = params.set('categorie', categorie);
    }
    return this.http.get<readonly Ticket[]>(this.base, { params });
  }

  parId(id: string): Observable<Ticket> {
    return this.http.get<Ticket>(`${this.base}/${id}`);
  }

  escalade(id: string): Observable<Escalade> {
    return this.http.get<Escalade>(`${this.base}/${id}/escalade`);
  }
}
