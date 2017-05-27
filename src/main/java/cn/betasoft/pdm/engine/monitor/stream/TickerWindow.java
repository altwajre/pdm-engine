package cn.betasoft.pdm.engine.monitor.stream;

public class TickerWindow {

	String ticker;

	long timestamp;

	public TickerWindow(String ticker, long timestamp) {
		this.ticker = ticker;
		this.timestamp = timestamp;
	}
}