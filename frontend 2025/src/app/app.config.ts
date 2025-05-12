import { ApplicationConfig, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { HttpClientModule, provideHttpClient, withInterceptors } from '@angular/common/http';
import { authInterceptor } from './interceptors/auth.interceptor';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideToastr } from 'ngx-toastr';
export const appConfig: ApplicationConfig = {
  providers: [
    
    provideAnimations(),
    provideToastr({
      timeOut: 3000, 
      preventDuplicates: true,
      enableHtml:true,
      
      countDuplicates:true,
      closeButton:true,
      positionClass:'toast-bottom-right'}),
    provideRouter(routes),
    importProvidersFrom(HttpClientModule),
    provideHttpClient(withInterceptors([authInterceptor]))
  ]
};
