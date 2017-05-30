package cn.betasoft.pdm.engine.monitor.stream;

public class TickerWindow {

	String ticker;

	long timestamp;

	public TickerWindow(String ticker, long timestamp) {
		this.ticker = ticker;
		this.timestamp = timestamp;
	}

	public TickerWindow() {
	}

	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
}