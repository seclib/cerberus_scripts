/*
 * Copyright (c) 2018 Nike, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.qos.logback.core.rolling;

import com.nike.cerberus.service.ConfigService;
import com.nike.cerberus.service.S3LogUploaderService;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Stream;

/**
 * Rolling policy that will copy audit logs to S3 if enabled when the logs roll.
 */
public class AuditLogsS3TimeBasedRollingPolicy<E> extends TimeBasedRollingPolicy<E> {

    private LinkedBlockingQueue<String> logChunkFileS3Queue = new LinkedBlockingQueue<>();

    private S3LogUploaderService s3LogUploaderService = null;

    public void setS3LogUploaderService(S3LogUploaderService s3LogUploaderService) {
        this.s3LogUploaderService = s3LogUploaderService;
        if (logChunkFileS3Queue.size() > 0) {
            Stream.generate(() -> logChunkFileS3Queue.poll()).forEach(s3LogUploaderService::ingestLog);
        }
    }

    @Override
    public void rollover() throws RolloverFailure {
        super.rollover();

        if (ConfigService.getInstance().isS3AuditLogCopyingEnabled()) {
            String filename = timeBasedFileNamingAndTriggeringPolicy.getElapsedPeriodsFileName() + ".gz";
            if (s3LogUploaderService != null) {
                s3LogUploaderService.ingestLog(filename);
            } else {
                logChunkFileS3Queue.offer(filename);
            }
        }
    }
}
