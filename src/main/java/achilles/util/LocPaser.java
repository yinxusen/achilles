package achilles.util;

public class LocPaser {
	/**
	 * parse locinfo
	 * @param locinfo
	 * @return lat lng
	 */
	public static double[] parseLoc(String locinfo){
		double[] ret = new double[2];
		String[] tmpret = locinfo.split(",");
		if(tmpret.length<2)
			return null;
		ret[0] = Double.valueOf(tmpret[0]);
		ret[1] = Double.valueOf(tmpret[1]);
		return ret;
	}
}
//console and query by file
