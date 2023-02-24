package pers.laineyc.blackbox.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

@Slf4j
@Component
public class LoadLockService {

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final Lock writeLock = readWriteLock.writeLock();

    private final Lock readLock = readWriteLock.readLock();

    public <T> T writeAction(Supplier<T> supplier) {
        writeLock.lock();
        try {
            return supplier.get();
        }
        finally {
            writeLock.unlock();
        }
    }

    public <T> T readAction(Supplier<T> supplier) {
        readLock.lock();
        try {
            return supplier.get();
        }
        finally {
            readLock.unlock();
        }
    }

}
