import {Component, HostBinding, OnInit, signal} from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {ButtonOverviewExample} from './button/button.component';
import {CardMediaSizeExample} from './card/card';
import {OverlayContainer} from "@angular/cdk/overlay";
import {MatToolbar} from '@angular/material/toolbar';
import {MatIconModule} from '@angular/material/icon';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ButtonOverviewExample, CardMediaSizeExample, MatIconModule, MatToolbar],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App implements OnInit {
  @HostBinding('class.dark-theme') get darkClass() { return this.dark; }
  dark = false;

  constructor(private overlay: OverlayContainer) {}

  ngOnInit() {
    this.dark = localStorage.getItem('darkMode') === 'true';
    this.applyOverlay();
  }

  toggleTheme() {
    this.dark = !this.dark;
    localStorage.setItem('darkMode', String(this.dark));
    this.applyOverlay();
  }

  private applyOverlay() {
    const cls = 'dark-theme';
    const overlayClasses = this.overlay.getContainerElement().classList;
    this.dark ? overlayClasses.add(cls) : overlayClasses.remove(cls);
  }
}
