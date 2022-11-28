/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.type.descriptor.java.spi;

import org.hibernate.type.BasicType;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractClassJavaType;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;

/**
 * Java type for embeddable aggregates, which allows resolving a recommended {@link JdbcType}.
 *
 * @author Christian Beikov
 */
public class EmbeddableAggregateJavaType<T> extends AbstractClassJavaType<T> {

	private final String structName;

	public EmbeddableAggregateJavaType(Class<T> type, String structName) {
		super( type );
		this.structName = structName;
	}

	@Override
	public JdbcType getRecommendedJdbcType(JdbcTypeIndicators context) {
		final BasicType<T> basicType = context.getTypeConfiguration().getBasicTypeForJavaType( getJavaType() );
		if ( basicType != null ) {
			return basicType.getJdbcType();
		}
		if ( structName != null ) {
			return context.getJdbcType( SqlTypes.STRUCT );
		}
		// prefer json by default for now
		final JdbcType descriptor = context.getJdbcType( SqlTypes.JSON );
		if ( descriptor != null ) {
			return descriptor;
		}
		throw new JdbcTypeRecommendationException(
				"Could not determine recommended JdbcType for `" + getJavaType().getTypeName() + "`"
		);
	}

	@Override
	public String toString(T value) {
		return value.toString();
	}

	@Override
	public T fromString(CharSequence string) {
		throw new UnsupportedOperationException(
				"Conversion from String strategy not known for this Java type : " + getJavaType().getTypeName()
		);
	}

	@Override
	public <X> X unwrap(T value, Class<X> type, WrapperOptions options) {
		if ( type.isAssignableFrom( getJavaTypeClass() ) ) {
			//noinspection unchecked
			return (X) value;
		}
		throw new UnsupportedOperationException(
				"Unwrap strategy not known for this Java type : " + getJavaType().getTypeName()
		);
	}

	@Override
	public <X> T wrap(X value, WrapperOptions options) {
		if ( getJavaTypeClass().isInstance( value ) ) {
			//noinspection unchecked
			return (T) value;
		}
		throw new UnsupportedOperationException(
				"Wrap strategy not known for this Java type : " + getJavaType().getTypeName()
		);
	}

	@Override
	public String toString() {
		return "BasicJavaType(" + getJavaType().getTypeName() + ")";
	}
}
