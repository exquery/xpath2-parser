/*
 * XPath 2 Parser
 * A Parser for XPath 2
 * Copyright (C) 2016 Evolved Binary Ltd.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.evolvedbinary.functional;

import java.util.NoSuchElementException;

/**
 * A disjunction, more basic than but similar to {@link scala.util.Either}
 * in addition it is also right-biased in a similar way to {@link scalaz.\/}
 *
 * @param <L> Type of left-hand-side parameter
 * @param <R> Type of right-hand-side parameter
 *
 * @author Adam Retter <adam.retter@googlemail.com>
 */
public abstract class Either<L, R> {
    
    private final boolean isLeft;
    
    Either(final boolean isLeft) {
        this.isLeft = isLeft;
    }
    
    public final boolean isLeft() {
        return isLeft;
    }

    public final boolean isRight() {
        return !isLeft;
    }
    
    public final LeftProjection<L, R> left() {
        return new LeftProjection(this);
    }
    
    public final RightProjection<L, R> right() {
        return new RightProjection(this);
    }

    /**
     * Map on the right-hand-side of the disjunction
     *
     * @param f The function to map with
     */
    public final <T> Either<L, T> map(final Function<R, T> f) {
        if(isLeft()) {
            return (Left<L, T>)this;
        } else {
            return Right(f.apply(((Right<L, R>)this).value));
        }
    }

    /**
     * Bind through on the right-hand-side of this disjunction
     *
     * @param f the function to bind through
     */
    public final <LL extends L, T> Either<LL, T> flatMap(final Function<R, Either<LL, T>> f) {
        if(isLeft) {
            return (Left<LL, T>)this;
        } else {
            return f.apply(((Right<L, R>)this).value);
        }
    }

    /**
     * Map on the left-hand-side of the disjunction
     *
     * @param f The function to map with
     */
    public final <T> Either<T, R> leftMap(final Function<L, T> f) {
        if(isLeft) {
            return Left(f.apply(((Left<L, R>)this).value));
        } else {
            return (Right<T, R>)this;
        }
    }
    
    /**
     * Catamorphism. Run the first given function if left,
     * otherwise the second given function
     * 
     * 
     * @param <T> The result type from performing the fold
     * @param lf A function that may be applied to the left-hand-side
     * @param rf A function that may be applied to the right-hand-side
     */
    public final <T> T fold(final Function<L, T> lf, final Function<R, T> rf) {
        if(isLeft) {
            return lf.apply(((Left<L, R>)this).value);
        } else {
            return rf.apply(((Right<L, R>)this).value);
        }
    }

    /**
     * Return the value from the right-hand-side of this disjunction or
     * run the function on the left-hand-side
     * 
     * @param <RR> The result type
     * @param lf A function that may be applied to the left-hand-side
     */
    public final <RR extends R> RR valueOr(final Function<L, RR> lf) {
        if(isLeft) {
            return lf.apply(((Left<L, R>)this).value);
        } else {
            return ((Right<L, RR>)this).value;
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj != null && obj instanceof Either) {
            final Either other = (Either)obj;

            if(other.isLeft && this.isLeft) {
                return other.left().get().equals(this.left().get());
            } else if((!other.isLeft) && (!this.isLeft)) {
                return other.right().get().equals(this.right().get());
            }
        }

        return false;
    }

    public final static <L, R> Either<L, R> Left(final L value) {
        return new Left<L, R>(value);
    }

    public final static <L, R> Either<L, R> Right(final R value) {
        return new Right<L, R>(value);
    }

    public final static class Left<L, R> extends Either<L, R> {
        final L value;
        private Left(final L value) {
            super(true);
            this.value = value;
        }
    }
    
    public final static class Right<L, R> extends Either<L, R> {
        final R value;
        private Right(final R value) {
            super(false);
            this.value = value;
        }
    }
    
    public final class LeftProjection<L, R> {
        final Either<L, R> e;
        private LeftProjection(final Either<L, R> e) {
            this.e = e;
        }
        
        public final L get() {
            if(e.isLeft()) {
                return ((Left<L, R>)e).value;
            } else {
                throw new NoSuchElementException("Either.left value on Right");
            }
        }
    }
    
    public final class RightProjection<L, R> {
        final Either<L, R> e;
        private RightProjection(final Either<L, R> e) {
            this.e = e;
        }
        
        public final R get() {
            if(e.isRight()) {
                return ((Right<L, R>)e).value;
            } else {
                throw new NoSuchElementException("Either.right value on Left");
            }
        }
    }
}
