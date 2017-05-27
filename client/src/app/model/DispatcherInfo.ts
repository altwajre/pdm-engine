export class DispatcherInfo {

    sampleTime: Date;
    parallelism: number;
    activeThreadCount: number;
    queuedTaskCount: number;
    poolSize: number;
    runningThreadCount: number;
    queuedSubmissionCount: number;

    constructor() { }
}