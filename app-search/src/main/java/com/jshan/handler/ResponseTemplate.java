package com.jshan.handler;

/**
 * !!!Description here
 *
 * @author : jshan
 * @created : 2023/07/18
 */
public record ResponseTemplate<T>(String status, T message) {

}
