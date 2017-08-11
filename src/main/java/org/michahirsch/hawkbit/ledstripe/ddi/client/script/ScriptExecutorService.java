package org.michahirsch.hawkbit.ledstripe.ddi.client.script;

import java.io.File;

public interface ScriptExecutorService {

    void execute(String controllerId, File file);

}
