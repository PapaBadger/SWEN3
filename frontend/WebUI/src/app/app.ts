import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {ButtonOverviewExample} from './button/button.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ButtonOverviewExample],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('WebUI');
}
