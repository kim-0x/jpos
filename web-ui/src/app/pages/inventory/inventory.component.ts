import { Component } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';

@Component({
  selector: 'app-inventory',
  imports: [MatCardModule, MatProgressBarModule],
  templateUrl: './inventory.component.html',
  styleUrl: './inventory.component.scss'
})
export class InventoryComponent {
  readonly stockHealth = [
    { item: 'Premium Rice 5kg', level: 72 },
    { item: 'Cooking Oil 2L', level: 34 },
    { item: 'Laundry Soap', level: 18 }
  ];
}
