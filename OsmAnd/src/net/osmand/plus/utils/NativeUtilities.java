package net.osmand.plus.utils;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.util.Pair;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.osmand.core.android.MapRendererView;
import net.osmand.core.jni.AreaI;
import net.osmand.core.jni.ColorARGB;
import net.osmand.core.jni.FColorARGB;
import net.osmand.core.jni.FColorRGB;
import net.osmand.core.jni.PointI;
import net.osmand.core.jni.SWIGTYPE_p_sk_spT_SkImage_const_t;
import net.osmand.core.jni.SwigUtilities;
import net.osmand.core.jni.TileId;
import net.osmand.core.jni.TileIdList;
import net.osmand.core.jni.Utilities;
import net.osmand.data.LatLon;
import net.osmand.data.RotatedTileBox;
import net.osmand.plus.plugins.weather.OfflineForecastHelper;
import net.osmand.util.MapUtils;

import java.util.List;

public class NativeUtilities {

	public static final int MIN_ALTITUDE_VALUE = -20_000;

	public static SWIGTYPE_p_sk_spT_SkImage_const_t createSkImageFromBitmap(@NonNull Bitmap inputBmp) {
		return SwigUtilities.createSkImageARGB888With(
				inputBmp.getWidth(), inputBmp.getHeight(), AndroidUtils.getByteArrayFromBitmap(inputBmp));
	}

	public static FColorRGB createFColorRGB(@ColorInt int color) {
		return new FColorRGB((color >> 16 & 0xff) / 255.0f,
				((color >> 8) & 0xff) / 255.0f,
				((color) & 0xff) / 255.0f);
	}

	public static FColorARGB createFColorARGB(@ColorInt int color) {
		float a = (color >> 24) & 0xFF;
		float r = (color >> 16) & 0xFF;
		float g = (color >> 8) & 0xFF;
		float b = (color) & 0xFF;
		return new FColorARGB(a / 255, r / 255, g / 255, b / 255);
	}

	public static ColorARGB createColorARGB(@ColorInt int color) {
		int a = (color >> 24) & 0xFF;
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = (color) & 0xFF;
		return new ColorARGB((short)a, (short)r , (short)g, (short)b);
	}

	public static ColorARGB createColorARGB(@ColorInt int color, int alpha) {
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = (color) & 0xFF;
		return new ColorARGB((short)alpha, (short)r , (short)g, (short)b);
	}

	@Nullable
	public static PointI get31FromPixel(@NonNull MapRendererView mapRenderer, @Nullable RotatedTileBox tileBox,
	                                    int x, int y) {
		return get31FromPixel(mapRenderer, tileBox, new PointI(x, y), false);
	}

	@Nullable
	public static PointI get31FromPixel(@NonNull MapRendererView mapRenderer, @Nullable RotatedTileBox tileBox,
	                                    int x, int y, boolean useShiftedCenter) {
		return get31FromPixel(mapRenderer, tileBox, new PointI(x, y), useShiftedCenter);
	}

	@Nullable
	public static PointI get31FromPixel(@NonNull MapRendererView mapRenderer, @Nullable RotatedTileBox tileBox,
	                                    @NonNull PointI screenPoint, boolean useShiftedCenter) {
		if (useShiftedCenter && tileBox != null && tileBox.isCenterShifted()) {
			RotatedTileBox tbCenter = tileBox.copy();
			tbCenter.setCenterLocation(0.5f, 0.5f);
			int x = screenPoint.getX() + (tileBox.getCenterPixelX() - tbCenter.getCenterPixelX());
			int y = screenPoint.getY() + (tileBox.getCenterPixelY() - tbCenter.getCenterPixelY());
			screenPoint = new PointI(x, y);
		}
		PointI point31 = new PointI();
		if (mapRenderer.getLocationFromScreenPoint(screenPoint, point31)) {
			return point31;
		}
		return null;
	}

	@Nullable
	public static PointI get31FromElevatedPixel(@NonNull MapRendererView mapRenderer, int x, int y) {
		PointI point31 = new PointI();
		return mapRenderer.getLocationFromElevatedPoint(new PointI(x, y), point31)
				? point31
				: null;
	}

	@Nullable
	public static PointI get31FromElevatedPixel(@NonNull MapRendererView mapRenderer, float x, float y) {
		return get31FromElevatedPixel(mapRenderer, (int) x, (int) y);
	}

	@NonNull
	public static LatLon getLatLonFromElevatedPixel(@Nullable MapRendererView mapRenderer,
	                                                @NonNull RotatedTileBox tileBox,
	                                                @NonNull PointF pixel) {
		return getLatLonFromElevatedPixel(mapRenderer, tileBox, pixel.x, pixel.y);
	}

	@NonNull
	public static LatLon getLatLonFromElevatedPixel(@Nullable MapRendererView mapRenderer,
	                                                @NonNull RotatedTileBox tileBox,
	                                                float x, float y) {
		return getLatLonFromElevatedPixel(mapRenderer, tileBox, (int) x, (int) y);
	}

	@NonNull
	public static LatLon getLatLonFromElevatedPixel(@Nullable MapRendererView mapRenderer,
	                                                @NonNull RotatedTileBox tileBox,
	                                                int x, int y) {
		PointI point31 = null;
		if (mapRenderer != null) {
			point31 = get31FromElevatedPixel(mapRenderer, x, y);
		}

		if (point31 == null) {
			return tileBox.getLatLonFromPixel(x, y);
		}

		double lat = MapUtils.get31LatitudeY(point31.getY());
		double lon = MapUtils.get31LongitudeX(point31.getX());
		return new LatLon(lat, lon);
	}

	@Nullable
	public static LatLon getLatLonFromPixel(@Nullable MapRendererView mapRenderer, @Nullable RotatedTileBox tileBox,
	                                        int x, int y) {
		if (mapRenderer == null) {
			return tileBox != null ? tileBox.getLatLonFromPixel(x, y) : null;
		}
		return getLatLonFromPixel(mapRenderer, tileBox, new PointI(x, y));
	}

	@Nullable
	public static LatLon getLatLonFromPixel(@NonNull MapRendererView mapRenderer, @Nullable RotatedTileBox tileBox,
	                                        @NonNull PointI screenPoint) {
		PointI point31 = get31FromPixel(mapRenderer, tileBox, screenPoint, false);
		if (point31 != null) {
			return new LatLon(MapUtils.get31LatitudeY(point31.getY()), MapUtils.get31LongitudeX(point31.getX()));
		}
		return null;
	}

	@NonNull
	public static LatLon getLatLonFromPixel(@Nullable MapRendererView mapRenderer, @NonNull RotatedTileBox tileBox,
	                                        float x, float y) {
		LatLon latLon = mapRenderer != null
				? getLatLonFromPixel(mapRenderer, tileBox, new PointI((int) x, (int) y)) : null;
		if (latLon == null) {
			latLon = tileBox.getLatLonFromPixel(x, y);
		}
		return latLon;
	}

	public static Double getAltitudeForLatLon(@Nullable MapRendererView mapRenderer, @Nullable LatLon latLon) {
		if (latLon != null) {
			return getAltitudeForLatLon(mapRenderer, latLon.getLatitude(), latLon.getLongitude());
		}
		return null;
	}

	public static Double getAltitudeForLatLon(@Nullable MapRendererView mapRenderer, double lat, double lon) {
		int x = MapUtils.get31TileNumberX(lon);
		int y = MapUtils.get31TileNumberY(lat);
		return getAltitudeForElevatedPoint(mapRenderer, new PointI(x, y));
	}

	public static Double getAltitudeForPixelPoint(@Nullable MapRendererView mapRenderer, @Nullable PointI screenPoint) {
		if (mapRenderer != null && screenPoint != null) {
			PointI elevatedPoint = new PointI();
			if (mapRenderer.getLocationFromElevatedPoint(screenPoint, elevatedPoint)) {
				return getAltitudeForElevatedPoint(mapRenderer, elevatedPoint);
			}
		}
		return null;
	}

	public static Double getAltitudeForElevatedPoint(@Nullable MapRendererView mapRenderer, @Nullable PointI elevatedPoint) {
		double altitude = MIN_ALTITUDE_VALUE;
		if (mapRenderer != null && elevatedPoint != null) {
			altitude = mapRenderer.getLocationHeightInMeters(elevatedPoint);
		}
		return altitude > MIN_ALTITUDE_VALUE ? altitude : null;
	}

	@NonNull
	public static PointF getPixelFromLatLon(@Nullable MapRendererView mapRenderer, @NonNull RotatedTileBox tileBox,
	                                        double lat, double lon) {
		PointI screenPoint = getScreenPointFromLatLon(mapRenderer, lat, lon);
		if (screenPoint != null) {
			return new PointF(screenPoint.getX(), screenPoint.getY());
		} else {
			return new PointF(tileBox.getPixXFromLatLon(lat, lon), tileBox.getPixYFromLatLon(lat, lon));
		}
	}

	@Nullable
	public static PointI getScreenPointFromLatLon(@Nullable MapRendererView mapRenderer, double lat, double lon) {
		if (mapRenderer != null) {
			int x31 = MapUtils.get31TileNumberX(lon);
			int y31 = MapUtils.get31TileNumberY(lat);
			PointI screenPoint = new PointI();
			if (mapRenderer.getScreenPointFromLocation(new PointI(x31, y31), screenPoint, true)) {
				return screenPoint;
			}
		}
		return null;
	}

	@NonNull
	public static PointF getPixelFrom31(@Nullable MapRendererView mapRenderer, @NonNull RotatedTileBox tileBox,
	                                    @NonNull PointI point31) {
		int x31 = point31.getX();
		int y31 = point31.getY();
		PointF point = null;
		if (mapRenderer != null) {
			PointI screenPoint = new PointI();
			if (mapRenderer.getScreenPointFromLocation(new PointI(x31, y31), screenPoint, true)) {
				point = new PointF(screenPoint.getX(), screenPoint.getY());
			}
		}
		if (point == null) {
			point = new PointF(tileBox.getPixXFrom31(x31, y31), tileBox.getPixYFrom31(x31, y31));
		}
		return point;
	}

	@NonNull
	public static PointF getElevatedPixelFromLatLon(@Nullable MapRendererView mapRenderer,
	                                                @NonNull RotatedTileBox tileBox,
	                                                @NonNull LatLon latLon) {
		return getElevatedPixelFromLatLon(mapRenderer, tileBox, latLon.getLatitude(), latLon.getLongitude());
	}

	@NonNull
	public static PointF getElevatedPixelFromLatLon(@Nullable MapRendererView mapRenderer,
	                                                @NonNull RotatedTileBox tileBox,
	                                                double lat, double lon) {
		int x31 = MapUtils.get31TileNumberX(lon);
		int y31 = MapUtils.get31TileNumberY(lat);
		return getElevatedPixelFrom31(mapRenderer, tileBox, x31, y31);
	}

	@NonNull
	public static PointF getElevatedPixelFrom31(@Nullable MapRendererView mapRenderer,
	                                            @NonNull RotatedTileBox tileBox,
	                                            int x31, int y31) {
		PointF pixel = null;

		if (mapRenderer != null) {
			PointI point31 = new PointI(x31, y31);
			PointI screenPoint = new PointI();
			if (mapRenderer.getElevatedPointFromLocation(point31, screenPoint, true)) {
				pixel = new PointF(screenPoint.getX(), screenPoint.getY());
			}
		}

		if (pixel == null) {
			float pixX = tileBox.getPixXFrom31(x31, y31);
			float pixY = tileBox.getPixYFrom31(x31, y31);
			pixel = new PointF(pixX, pixY);
		}

		return pixel;
	}

	@NonNull
	public static PointI calculateTarget31(@NonNull MapRendererView mapRenderer,
	                                       double latitude, double longitude, boolean applyNewTarget) {
		PointI target31 = new PointI(MapUtils.get31TileNumberX(longitude), MapUtils.get31TileNumberY(latitude));
		if (applyNewTarget) {
			mapRenderer.setTarget(target31);
		}
		return target31;
	}

	public static boolean containsLatLon(@Nullable MapRendererView mapRenderer, @NonNull RotatedTileBox tileBox,
	                                     @NonNull LatLon latLon) {
		return containsLatLon(mapRenderer, tileBox, latLon.getLatitude(), latLon.getLongitude());
	}

	public static boolean containsLatLon(@Nullable MapRendererView mapRenderer, @NonNull RotatedTileBox tileBox,
	                                     double latitude, double longitude) {
		if (mapRenderer != null) {
			return mapRenderer.isPositionVisible(new PointI(MapUtils.get31TileNumberX(longitude),
					MapUtils.get31TileNumberY(latitude)));
		} else {
			return tileBox.containsLatLon(latitude, longitude);
		}
	}

	@NonNull
	public static PointI getPoint31FromLatLon(@NonNull LatLon latLon) {
		return getPoint31FromLatLon(latLon.getLatitude(), latLon.getLongitude());
	}

	@NonNull
	public static PointI getPoint31FromLatLon(double lat, double lon) {
		int x31 = MapUtils.get31TileNumberX(lon);
		int y31 = MapUtils.get31TileNumberY(lat);
		return new PointI(x31, y31);
	}

	@Nullable
	public static Pair<PointF, PointF> clipLineInVisibleRect(@NonNull MapRendererView mapRenderer,
	                                                         @NonNull RotatedTileBox tileBox,
	                                                         @NonNull PointI start31,
	                                                         @NonNull PointI end31) {
		AreaI screenBbox = mapRenderer.getVisibleBBox31();
		PointI clippedStart31 = null;
		PointI clippedEnd31 = null;
		if (screenBbox.contains(start31)) {
			clippedStart31 = start31;
		}
		if (screenBbox.contains(end31)) {
			clippedEnd31 = end31;
		}
		if (clippedStart31 == null && clippedEnd31 == null) {
			clippedStart31 = new PointI(0, 0);
			clippedEnd31 = new PointI(0, 0);
			if (Utilities.calculateIntersection(start31, end31, screenBbox, clippedStart31)) {
				Utilities.calculateIntersection(end31, start31, screenBbox, clippedEnd31);
			} else {
				return null;
			}
		} else if (clippedStart31 == null) {
			clippedStart31 = new PointI(0, 0);
			if (!Utilities.calculateIntersection(start31, end31, screenBbox, clippedStart31)) {
				return null;
			}
		} else if (clippedEnd31 == null) {
			clippedEnd31 = new PointI(0, 0);
			if (!Utilities.calculateIntersection(end31, start31, screenBbox, clippedEnd31)) {
				return null;
			}
		}
		PointF startPixel = NativeUtilities.getElevatedPixelFrom31(mapRenderer, tileBox,
				clippedStart31.getX(), clippedStart31.getY());
		PointF endPixel = NativeUtilities.getElevatedPixelFrom31(mapRenderer, tileBox,
				clippedEnd31.getX(), clippedEnd31.getY());
		return Pair.create(startPixel, endPixel);
	}

	public static TileIdList convertToQListTileIds(@NonNull List<Long> tileIds) {
		TileIdList qTileIds = new TileIdList();
		for (Long tileId : tileIds) {
			qTileIds.add(TileId.fromXY(OfflineForecastHelper.getTileX(tileId),
					OfflineForecastHelper.getTileY(tileId)));
		}
		return qTileIds;
	}
}
