package cn.betasoft.pdm.engine.scheduler;

public enum CronValue {

	FIFTEN_SECOND("0/15 * * * * ?"), THIRTY_SECOND("0/30 * * * * ?"), SIXTY_SECOND("0/60 * * * * ?"), ONE_MINUTE(
			"0 0/1 * * * ?"), TWO_MINUTE("0 0/2 * * * ?"), THREE_MINUTE("0 0/3 * * * ?"), FIVE_MINUTE(
					"0 0/5 * * * ?"), TEN_MINUTE("0 0/10 * * * ?"), FIFTEN_MINUTE("0 0/15 * * * ?"), THIRTY_MINUTE(
							"0 0/30 * * * ?"), ONE_HOUR("0 0 0/1 * * ?"), FOUR_HOUR(
									"0 0 0/4 * * ?"), SIX_HOUR("0 0 0/6 * * ?"), TWENTY_FOUR_HOUR("0 0 0/24 * * ?");

	private final String cronExpression;

	CronValue(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public static CronValue getValueByOrdinal(int ordinal) {
		if (ordinal == 0) {
			return CronValue.FIFTEN_SECOND;
		} else if (ordinal == 1) {
			return CronValue.THIRTY_SECOND;
		} else if (ordinal == 2) {
			return CronValue.SIXTY_SECOND;
		} else if (ordinal == 3) {
			return CronValue.ONE_MINUTE;
		} else if (ordinal == 4) {
			return CronValue.TWO_MINUTE;
		} else if (ordinal == 5) {
			return CronValue.THIRTY_MINUTE;
		} else if (ordinal == 6) {
			return CronValue.FIVE_MINUTE;
		} else if (ordinal == 7) {
			return CronValue.TEN_MINUTE;
		} else if (ordinal == 8) {
			return CronValue.FIFTEN_MINUTE;
		} else if (ordinal == 9) {
			return CronValue.THIRTY_MINUTE;
		} else if (ordinal == 10) {
			return CronValue.ONE_HOUR;
		} else if (ordinal == 11) {
			return CronValue.FOUR_HOUR;
		} else if (ordinal == 12) {
			return CronValue.SIX_HOUR;
		} else if (ordinal == 13) {
			return CronValue.TWENTY_FOUR_HOUR;
		} else {
			return null;
		}
	}
}
