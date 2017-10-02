package cz.novros.cp.common.service;

import java.util.Collection;

import javax.annotation.Nonnull;

import cz.novros.cp.common.entity.Parcel;

/**
 * This interface defines method over parcel service in CP system.
 */
public interface ParcelService {

	/**
	 * Read parcel for tracking numbers.
	 *
	 * @param trackingNumbers Tracking numbers for which will be parcels read.
	 *
	 * @return Parcels with given tracking numbers.
	 */
	@Nonnull
	Collection<Parcel> readParcels(@Nonnull final Collection<String> trackingNumbers);

	/**
	 * Refresh parcels by tracking number.
	 *
	 * @param trackingNumbers Tracking number for which will be parcel updated.
	 *
	 * @return Updated parcels given by tracking numbers.
	 */
	@Nonnull
	Collection<Parcel> refreshParcels(@Nonnull final Collection<String> trackingNumbers);
}
