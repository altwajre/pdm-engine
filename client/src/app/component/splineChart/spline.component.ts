import {
    Component,
    Input,
    ElementRef,
    AfterViewInit,
    OnInit,
    OnDestroy,
    ViewChild
} from '@angular/core';

import * as Highcharts from "highcharts";

@Component({
    selector: 'spline-chart',
    templateUrl: './spline.component.html',
    styleUrls: ['./spline.component.scss']
})
export class SplineComponent implements OnInit, AfterViewInit, OnDestroy {

    @Input() title: string;

    @Input() seriesNames: any;

    @Input() ytitle: string;

    @ViewChild('chart') public chartEl: ElementRef;

    private _chart: Highcharts.ChartObject;

    constructor() { }

    @Input()
    set initPoints(points: any) {
        if (points && this._chart) {
            for (var point of points) {
                for (let childIndex in point) {
                    // http://api.highcharts.com/highcharts/Series.addPoint
                    this._chart.series[+childIndex].addPoint(point[childIndex], false, false);
                }
            }
            this._chart.redraw();
        }
    }

    @Input()
    set newData(newData: any) {
        if (newData && this._chart) {
            let update = false;
            let pointObjects: Highcharts.PointObject[] = this._chart.series[0].data;
            for (let pointObject of pointObjects) {
                if (pointObject.x === newData[0][0]) {
                    update = true;
                    break;
                }
            }
            if (update) {
                for (let i = 0; i < this._chart.series.length; i++) {
                    let pointObjects: Highcharts.PointObject[] = this._chart.series[i].data;
                    for (let pointObject of pointObjects) {
                        if (pointObject.x === newData[i][0]) {
                            pointObject.update(newData[i]);
                            break;
                        }
                    }
                }
            } else {
                for (let childIndex in newData) {
                    this._chart.series[+childIndex].addPoint(newData[childIndex], false, true);
                }
                this._chart.redraw();
            }
        }
    }

    ngOnInit() {

    }

    ngAfterViewInit() {
        if (this.chartEl && this.chartEl.nativeElement) {
            let chartOptions: any = {
                chart: {
                    type: 'spline',
                    marginRight: 10,
                    renderTo: this.chartEl.nativeElement
                },
                credits: {
                    enabled: false
                },
                title: {
                    text: this.title,
                },
                xAxis: {
                    type: 'datetime',
                    tickPixelInterval: 150
                },
                yAxis: {
                    title: {
                        text: this.ytitle
                    },
                    plotLines: [{
                        value: 0,
                        width: 1,
                        color: '#808080'
                    }]
                },
                tooltip: {
                    formatter: function () {
                        let s = '<b>' + Highcharts.dateFormat('%H:%M:%S', this.x) + '</b>';
                        this.points.forEach(point => {
                            s += '<br/>' + point.series.name + ': ' +
                                point.y;
                        });
                        return s;
                    },
                    shared: true,
                    crosshairs: true
                },
                legend: {
                    enabled: true
                },
                exporting: {
                    enabled: false
                },
                plotOptions: {
                    spline: {
                        lineWidth: 2,
                        states: {
                            hover: {
                                lineWidth: 5
                            }
                        },
                        marker: {
                            enabled: true
                        }
                    }
                },
                series: []
            };
            for (var seriesName of this.seriesNames) {
                chartOptions.series.push({
                    name: seriesName,
                    data: []
                });
            }
            this._chart = new Highcharts.Chart(chartOptions);
        }
    }

    ngOnDestroy() {
        this._chart.destroy();
    }

}
