/*
 * Copyright 2009 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.netty.handler.codec.embedder;

import java.util.Collection;

/**
 * A helper that wraps an encoder or a decoder (codec) so that they can be used
 * without doing actual I/O in unit tests or higher level codecs.  Please refer
 * to {@link EncoderEmbedder} and {@link DecoderEmbedder} for more information.
 *
 * @author The Netty Project (netty-dev@lists.jboss.org)
 * @author Trustin Lee (trustin@gmail.com)
 * @version $Rev: 1862 $, $Date: 2009-11-02 21:01:19 +0900 (월, 02 11 2009) $
 */
public interface CodecEmbedder<E> {
    /**
     * Offers an input object to the pipeline of this embedder.
     *
     * @return {@code true} if and only if there is something to read in the
     *         product queue (see {@link #poll()} and {@link #peek()})
     */
    boolean offer(Object input);

    /**
     * Signals the pipeline that the encoding or decoding has been finished and
     * no more data will be offered.
     *
     * @return {@code true} if and only if there is something to read in the
     *         product queue (see {@link #poll()} and {@link #peek()})
     */
    boolean finish();

    /**
     * Consumes an encoded or decoded output from the product queue. The output
     * object is generated by the offered input objects.
     *
     * @return an encoded or decoded object.
     *         {@code null} if and only if there is no output object left in the
     *         product queue.
     */
    E poll();

    /**
     * Reads an encoded or decoded output from the head of the product queue.
     * The difference from {@link #poll()} is that it does not remove the
     * retrieved object from the product queue.
     *
     * @return an encoded or decoded object.
     *         {@code null} if and only if there is no output object left in the
     *         product queue.
     */
    E peek();

    /**
     * Consumes all encoded or decoded output from the product queue.  The
     * output object is generated by the offered input objects.  The behavior
     * of this method is identical with {@link Collection#toArray()} except that
     * the product queue is cleared.
     *
     * @return an array of all encoded or decoded objects.
     *         An empty array is returned if and only if there is no output
     *         object left in the product queue.
     */
    Object[] pollAll();

    /**
     * Consumes all encoded or decoded output from the product queue.  The
     * output object is generated by the offered input objects.  The behavior
     * of this method is identical with {@link Collection#toArray(Object[])}
     * except that the product queue is cleared.
     *
     * @return an array of all encoded or decoded objects.
     *         An empty array is returned if and only if there is no output
     *         object left in the product queue.
     */
    <T> T[] pollAll(T[] a);

    /**
     * Returns the number of encoded or decoded output in the product queue.
     */
    int size();
}
