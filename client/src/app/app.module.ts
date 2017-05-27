import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import { routing } from './app.routing';

import { ConfigService } from './services/config.service';
import { StompConfigService, StompService } from "@stomp/ng2-stompjs";
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { CoreModule } from './core/core.module';
import { ComponentModule } from './component/component.module';
import { AppComponent } from './app.component';
import { HomeComponent } from './home/home.component';
import { ListComponent } from './list/list.component';

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    ListComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    NgbModule.forRoot(),
    CoreModule,
    ComponentModule,
    routing
  ],
  providers: [
    StompService,
    {
      provide: StompConfigService,
      useClass: ConfigService
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
