import {
    ModuleWithProviders, NgModule,
    Optional, SkipSelf
} from '@angular/core';

import { PrettyJsonModule } from 'angular2-prettyjson';

import { SplineComponent } from './splineChart/spline.component';
import { BarchartComponent } from './barchart/barchart.component';
import { TreeComponent } from './tree/tree.component';

@NgModule({
    imports: [ PrettyJsonModule ],
    declarations: [ SplineComponent,BarchartComponent,TreeComponent ],
    exports: [ SplineComponent,BarchartComponent,TreeComponent ]
})
export class ComponentModule {

    constructor( @Optional() @SkipSelf() parentModule: ComponentModule) {
        if (parentModule) {
            throw new Error(
                'CompoentModule is already loaded. Import it in the AppModule only');
        }
    }
}