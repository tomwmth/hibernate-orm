/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.jdbc;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.BasicJavaType;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.internal.JdbcLiteralFormatterCharacterData;
import org.hibernate.type.descriptor.jdbc.spi.JdbcTypeRegistry;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * Descriptor for {@link Types#NVARCHAR NVARCHAR} handling.
 *
 * @author Steve Ebersole
 */
public class NVarcharJdbcType implements AdjustableJdbcType {
	public static final NVarcharJdbcType INSTANCE = new NVarcharJdbcType();

	public NVarcharJdbcType() {
	}

	@Override
	public int getJdbcTypeCode() {
		return Types.NVARCHAR;
	}

	@Override
	public String getFriendlyName() {
		return "NVARCHAR";
	}

	@Override
	public String toString() {
		return "NVarcharTypeDescriptor";
	}

	@Override
	public <T> JavaType<T> getJdbcRecommendedJavaTypeMapping(
			Integer length,
			Integer scale,
			TypeConfiguration typeConfiguration) {
		if ( length != null && length == 1 ) {
			return typeConfiguration.getJavaTypeRegistry().getDescriptor( Character.class );
		}
		return typeConfiguration.getJavaTypeRegistry().getDescriptor( String.class );
	}

	@Override
	public <T> JdbcLiteralFormatter<T> getJdbcLiteralFormatter(JavaType<T> javaType) {
		//noinspection unchecked
		return new JdbcLiteralFormatterCharacterData( javaType, true );
	}

	@Override
	public JdbcType resolveIndicatedType(
			JdbcTypeIndicators indicators,
			JavaType<?> domainJtd) {
		assert domainJtd != null;

		final TypeConfiguration typeConfiguration = indicators.getTypeConfiguration();
		final JdbcTypeRegistry jdbcTypeRegistry = typeConfiguration.getJdbcTypeRegistry();

		final int jdbcTypeCode;
		if ( indicators.isLob() ) {
			jdbcTypeCode = indicators.isNationalized() ? Types.NCLOB : Types.CLOB;
		}
		else {
			jdbcTypeCode = indicators.isNationalized() ? Types.NVARCHAR : Types.VARCHAR;
		}

		return jdbcTypeRegistry.getDescriptor( jdbcTypeCode );
	}

	@Override
	public Class<?> getPreferredJavaTypeClass(WrapperOptions options) {
		return String.class;
	}

	@Override
	public <X> ValueBinder<X> getBinder(final JavaType<X> javaType) {
		return new BasicBinder<>( javaType, this ) {
			@Override
			protected void doBind(PreparedStatement st, X value, int index, WrapperOptions options) throws SQLException {
				st.setNString( index, javaType.unwrap( value, String.class, options ) );
			}

			@Override
			protected void doBind(CallableStatement st, X value, String name, WrapperOptions options)
					throws SQLException {
				st.setNString( name, javaType.unwrap( value, String.class, options ) );
			}
		};
	}

	@Override
	public <X> ValueExtractor<X> getExtractor(final JavaType<X> javaType) {
		return new BasicExtractor<>( javaType, this ) {
			@Override
			protected X doExtract(ResultSet rs, int paramIndex, WrapperOptions options) throws SQLException {
				return javaType.wrap( rs.getNString( paramIndex ), options );
			}

			@Override
			protected X doExtract(CallableStatement statement, int index, WrapperOptions options) throws SQLException {
				return javaType.wrap( statement.getNString( index ), options );
			}

			@Override
			protected X doExtract(CallableStatement statement, String name, WrapperOptions options) throws SQLException {
				return javaType.wrap( statement.getNString( name ), options );
			}
		};
	}
}
