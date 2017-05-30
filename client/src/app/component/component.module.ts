import {
    ModuleWithProviders, NgModule,
    Optional, SkipSelf
} from '@angular/core';

import { SplineComponent } from './splineChart/spline.component';
import { BarchartComponent } from './barchart/barchart.component';

@NgModule({
    declarations: [ SplineComponent,BarchartComponent ],
    exports: [ SplineComponent,BarchartComponent ]
})
export class ComponentModule {

    constructor( @Optional() @SkipSelf() parentModule: ComponentModule) {
        if (parentModule) {
            throw new Error(
                'CompoentModule is already loaded. Import it in the AppModule only');
        }
    }
}