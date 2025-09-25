import {Component} from '@angular/core';
import {MatIconModule} from '@angular/material/icon';
import {MatDividerModule} from '@angular/material/divider';
import {MatButtonModule} from '@angular/material/button';
import {DataService} from '../DataService/DataService';

/**
 * @title Button overview
 */
@Component({
  selector: 'button-overview-example',
  templateUrl: 'button-component.html',
  styleUrl: 'button-component.css',
  standalone: true,
  imports: [MatButtonModule, MatIconModule, MatIconModule]
})
export class ButtonOverviewExample {
  constructor(private data: DataService) {
  }

  onGet(): void {
    this.data.getDocuments().subscribe({
      next: docs => console.log("Documents", docs),
      error: err => console.log("Sth went wrong", err),
    });
  }
}
