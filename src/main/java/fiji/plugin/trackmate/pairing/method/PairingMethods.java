package fiji.plugin.trackmate.pairing.method;

public enum PairingMethods
{

	COMMON_SPOTS( "Maximize N closest spots", new SpotConcensusPairing() ),
	CLOSEST_MEAN_POSITION( "Closest mean position", new AverageTrackPositionPairing() ),
	CLOSEST_MEDIAN_POSITION( "Closest median position", new MedianTrackPositionPairing() );

	private final String name;

	private final PairingMethod method;

	PairingMethods( final String name, final PairingMethod method )
	{
		this.name = name;
		this.method = method;
	}

	@Override
	public String toString()
	{
		return name;
	}

	public PairingMethod method()
	{
		return method;
	}
}
