import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient } from '@angular/common/http';
import { AppComponent } from './app/app.component';

// Standalone bootstrap (no NgModule). We only need the HTTP client globally;
// everything else is provided by the standalone components themselves.
bootstrapApplication(AppComponent, {
  providers: [provideHttpClient()],
}).catch((err) => console.error(err));
