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
import { CollectStat } from '../model/CollectStat';
import { MailBoxStat } from '../model/MailBoxStat';

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
  workDispatcherSeriesNames = ['激活线程数', '等候任务数'];

  // future dispatcher
  futureDispatcherInfos = [];
  newFutureDispatcherInfo = {};
  futureDispatcherSeriesNames = ['激活线程数', '等候任务数'];

  // collectStat
  collectStatInfos = [];
  newCollectStatInfo = {};
  collectStatSeriesNames = ['完成数', '超时数', '平均时间'];

  // mailboxStat
  mailboxStatInfos = [];
  newMailboxStatInfo = {};
  mailboxStatSeriesNames = ['新消息', '离开消息', '等候时间'];


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

    this._monitorService.findHeapInfo(2).subscribe(
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

    this._monitorService.findDispatcherInfo(this.dispatcherNames[1], 2).subscribe(
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

    this._monitorService.findDispatcherInfo(this.dispatcherNames[2], 2).subscribe(
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

    this._monitorService.findCollectStat(2).subscribe(
      infos => {
        this.collectStatInfos = [];
        if (infos && infos.length > 0) {
          for (var info of infos) {
            this.collectStatInfos.push({
              0: [info.sampleTime, info.totalNumber],
              1: [info.sampleTime, info.timeoutNumber],
              2: [info.sampleTime, info.avgTime]
            });
          }
        }
      },
      error => this.errorMessage = <any>error);

    this._monitorService.findMailboxStat(2).subscribe(
      infos => {
        this.mailboxStatInfos = [];
        if (infos && infos.length > 0) {
          for (var info of infos) {
            this.mailboxStatInfos.push({
              0: [info.sampleTime, info.entryNumber],
              1: [info.sampleTime, info.exitNumber],
              2: [info.sampleTime, info.avgTime]
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
    //console.log(monitorMessage);
    if (monitorMessage.type == MonitorType.HEAP) {
      let heapInfo = JSON.parse(monitorMessage.message) as HeapInfo;
      this.newHeapInfo = {
        0: [heapInfo.sampleTime, heapInfo.heapUsed],
        1: [heapInfo.sampleTime, heapInfo.heapCommitted]
      }
    } else if (monitorMessage.type == MonitorType.WORKDISPATCHER) {
      let dispatcherInfo = JSON.parse(monitorMessage.message) as DispatcherInfo;
      this.newWorkDispatcherInfo = {
        0: [dispatcherInfo.sampleTime, dispatcherInfo.activeThreadCount],
        1: [dispatcherInfo.sampleTime, dispatcherInfo.queuedSubmissionCount]
      }
    } else if (monitorMessage.type == MonitorType.FUTUREDISPATCHER) {
      let dispatcherInfo = JSON.parse(monitorMessage.message) as DispatcherInfo;
      this.newFutureDispatcherInfo = {
        0: [dispatcherInfo.sampleTime, dispatcherInfo.activeThreadCount],
        1: [dispatcherInfo.sampleTime, dispatcherInfo.queuedSubmissionCount]
      }
    } else if (monitorMessage.type == MonitorType.COLLECTSTAT) {
      let collectStat = JSON.parse(monitorMessage.message) as CollectStat;
      this.newCollectStatInfo = {
        0: [collectStat.sampleTime, collectStat.totalNumber],
        1: [collectStat.sampleTime, collectStat.timeoutNumber],
        2: [collectStat.sampleTime, collectStat.avgTime]
      }
    } else if (monitorMessage.type == MonitorType.MAILBOX) {
      let mailboxStat = JSON.parse(monitorMessage.message) as MailBoxStat;
      this.newMailboxStatInfo = {
        0: [mailboxStat.sampleTime, mailboxStat.entryNumber],
        1: [mailboxStat.sampleTime, mailboxStat.exitNumber],
        2: [mailboxStat.sampleTime, mailboxStat.avgTime]
      }
    }

  }

}
