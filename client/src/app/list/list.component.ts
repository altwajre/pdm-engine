import {
  Component,
  ElementRef,
  AfterViewInit,
  OnInit,
  OnDestroy,
  ViewChild
} from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { Message } from '@stomp/stompjs';

import { Subscription } from 'rxjs/Subscription';
import { StompService } from '@stomp/ng2-stompjs';

import { MonitorService } from '../core/monitor.service';
import { MonitorMessage } from '../model/MonitorMessage';
import { MonitorType } from '../model/MonitorType';
import { HeapInfo } from '../model/HeapInfo';
import { DispatcherInfo } from '../model/DispatcherInfo';

@Component({
  selector: 'app-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss']
})
export class ListComponent implements OnInit, AfterViewInit, OnDestroy {

  private _chart: Highcharts.ChartObject;


  // Stream of messages
  private subscription: Subscription;
  public messages: Observable<Message>;

  // Subscription status
  public subscribed: boolean;

  errorMessage: string;

  // heap info
  heapInfos = [];
  newHeapInfo = {};
  heapSeriesNames = ['使用', '提交'];

  dispatcherNames = ["akka.actor.default-dispatcher", "pdm-work-dispatcher",
    "pdm-future-dispatcher"];
     
  // work dispatcher   
  workDispatcherInfos = [];
  newWorkDispatcherInfo = {};
  workDispatcherSeriesNames = ['激活线程数','等候任务数'];

  // future dispatcher
  futureDispatcherInfos = [];
  newFutureDispatcherInfo = {};
  futureDispatcherSeriesNames = ['激活线程数','等候任务数'];


  /** Constructor */
  constructor(private _stompService: StompService,
    private _monitorService: MonitorService) { }

  ngOnInit() {

  }

  ngAfterViewInit() {
    this.subscribed = false;

    // Store local reference to Observable
    // for use with template ( | async )
    this.subscribe();

    this._monitorService.findHeapInfo(3).subscribe(
      infos => {
        this.heapInfos = [];
        if (infos && infos.length > 0) {
          for (var info of infos) {
            this.heapInfos.push({
              0: [info.sampleTime, info.heapUsed],
              1: [info.sampleTime, info.heapCommitted]
            });
          }
        }
      },
      error => this.errorMessage = <any>error);

      this._monitorService.findDispatcherInfo(this.dispatcherNames[1],3).subscribe(
      infos => {
        this.workDispatcherInfos = [];        
        if (infos && infos.length > 0) {
          for (var info of infos) {
            this.workDispatcherInfos.push({
              0: [info.sampleTime, info.activeThreadCount],
              1: [info.sampleTime, info.queuedSubmissionCount]
            });
          }
        }
      },
      error => this.errorMessage = <any>error);

      this._monitorService.findDispatcherInfo(this.dispatcherNames[2],3).subscribe(
      infos => {
        this.futureDispatcherInfos = [];
        if (infos && infos.length > 0) {
          for (var info of infos) {
            this.futureDispatcherInfos.push({
              0: [info.sampleTime, info.activeThreadCount],
              1: [info.sampleTime, info.queuedSubmissionCount]
            });
          }
        }
      },
      error => this.errorMessage = <any>error);
  }

  public subscribe() {
    if (this.subscribed) {
      return;
    }

    // Stream of messages
    this.messages = this._stompService.subscribe('/topic/monitor');

    // Subscribe a function to be run on_next message
    this.subscription = this.messages.subscribe(this.on_next);

    this.subscribed = true;
  }

  public unsubscribe() {
    if (!this.subscribed) {
      return;
    }

    // This will internally unsubscribe from Stomp Broker
    // There are two subscriptions - one created explicitly, the other created in the template by use of 'async'
    this.subscription.unsubscribe();
    this.subscription = null;
    this.messages = null;

    this.subscribed = false;
  }

  ngOnDestroy() {
    this.unsubscribe();
    this._chart.destroy();
  }

  public on_next = (message: Message) => {
    let monitorMessage = JSON.parse(message.body) as MonitorMessage;
    console.log(monitorMessage);
    if (monitorMessage.type == MonitorType.HEAP) {
      let heapInfo = JSON.parse(monitorMessage.message) as HeapInfo;
      this.newHeapInfo = {
        0: [heapInfo.sampleTime, heapInfo.heapUsed],
        1: [heapInfo.sampleTime, heapInfo.heapCommitted]
      }
    }else if (monitorMessage.type == MonitorType.WORKDISPATCHER) {
      let dispatcherInfo = JSON.parse(monitorMessage.message) as DispatcherInfo;
      this.newWorkDispatcherInfo = {
        0: [dispatcherInfo.sampleTime, dispatcherInfo.activeThreadCount],
        1: [dispatcherInfo.sampleTime, dispatcherInfo.queuedSubmissionCount]
      }
    }else if (monitorMessage.type == MonitorType.FUTUREDISPATCHER) {
      let dispatcherInfo = JSON.parse(monitorMessage.message) as DispatcherInfo;
      this.newFutureDispatcherInfo = {
        0: [dispatcherInfo.sampleTime, dispatcherInfo.activeThreadCount],
        1: [dispatcherInfo.sampleTime, dispatcherInfo.queuedSubmissionCount]
      }
    }

  }

}
