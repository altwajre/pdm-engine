import { Component, OnInit } from '@angular/core';

import { MonitorService } from '../core/monitor.service';

import { EngineActorStatus } from '../model/EngineActorStatus';
import { TreeNodeType } from '../model/TreeNodeType';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

  private chartData: Array<any>;

  private device: EngineActorStatus;

  private deivceIp: string = '138.174.65.0';

  private errorMessage: string;

  constructor(private _monitorService: MonitorService) { }

  ngOnInit() {
    this.queryActorStatus();
  }

  queryActorStatus() {
    this._monitorService.queryActorTree(this.deivceIp).subscribe(
      result => {
        this.device = result;
      },
      error => this.errorMessage = <any>error);
  }

  generateData() {
    this.chartData = [];
    for (let i = 0; i < (8 + Math.floor(Math.random() * 10)); i++) {
      this.chartData.push([
        `Index ${i}`,
        Math.floor(Math.random() * 100)
      ]);
    }
  }
}