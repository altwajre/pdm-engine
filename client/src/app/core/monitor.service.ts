import { Injectable } from '@angular/core';
import { Headers, Http, RequestOptions } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';

import { HeapInfo } from '../model/HeapInfo';
import { DispatcherInfo } from '../model/DispatcherInfo';

@Injectable()
export class MonitorService {

    constructor(private http: Http) { }

    findHeapInfo(offsetMinute: number): Observable<HeapInfo[]> {
        return this.http
            .get('api/monitor/heap/query/' + offsetMinute)
            .map(response => {
                return response.json() as HeapInfo[]
            });
    }

    findDispatcherInfo(dispatcherName,offsetMinute: number): Observable<DispatcherInfo[]> {
        let url = 'api/monitor/dispatcher/'+dispatcherName+'/query/' + offsetMinute;
        return this.http
            .get(url)
            .map(response => {
                return response.json() as DispatcherInfo[]
            });
    }

}    