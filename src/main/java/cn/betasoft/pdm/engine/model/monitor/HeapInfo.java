package cn.betasoft.pdm.engine.model.monitor;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 系统运行堆信息
 */
public class HeapInfo implements Serializable {

	private Date sampleTime;

	private Long heap;

	private Long heapInit;

	private Long heapCommitted;

	private Long heapUsed;

	private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public HeapInfo() {

	}

	public Date getSampleTime() {
		return sampleTime;
	}

	public void setSampleTime(Date sampleTime) {
		this.sampleTime = sampleTime;
	}

	public Long getHeap() {
		return heap;
	}

	public void setHeap(Long heap) {
		this.heap = heap;
	}

	public Long getHeapInit() {
		return heapInit;
	}

	public void setHeapInit(Long heapInit) {
		this.heapInit = heapInit;
	}

	public Long getHeapCommitted() {
		return heapCommitted;
	}

	public void setHeapCommitted(Long heapCommitted) {
		this.heapCommitted = heapCommitted;
	}

	public Long getHeapUsed() {
		return heapUsed;
	}

	public void setHeapUsed(Long heapUsed) {
		this.heapUsed = heapUsed;
	}

	@Override
	public String toString() {
		return "HeapInfo{" + "sampleTime=" + sdf.format(sampleTime) + ", heap=" + heap + ", heapInit=" + heapInit
				+ ", heapCommitted=" + heapCommitted + ", heapUsed=" + heapUsed + '}';
	}
}
