/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2007,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 1998, 1999, 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: ReaperThread.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.coordinator;

import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.logging.tsLogger;

/**
 * Class to record transactions with non-zero timeout values, and
 * class to implement a transaction reaper thread which terminates
 * these transactions once their timeout elapses.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: ReaperThread.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class ReaperThread extends Thread
{

public ReaperThread (TransactionReaper arg)
    {
        super("Transaction Reaper");
	reaperObject = arg;
	sleepPeriod = reaperObject.checkingPeriod();
	_shutdown = false;
    }

public void run ()
    {
    	if (tsLogger.logger.isTraceEnabled()) {
            tsLogger.logger.trace("ReaperThread.run ()");
        }

    	for (;;)
    	{
    	    /*
    	     * Cannot assume we sleep for the entire period. We may
    	     * be interrupted. If we are, just run a check anyway and
    	     * ignore.
    	     */
    
            synchronized(reaperObject)
            {
                // test our condition -- things may have changed while we were checking

                if (_shutdown) {
                    return;
                }

		sleepPeriod = reaperObject.checkingPeriod();
        
                if (sleepPeriod > 0)
                {
                     try
                     {
                          if (tsLogger.logger.isTraceEnabled()) {
                              tsLogger.logger.trace("Thread "+Thread.currentThread()+" sleeping for "+Long.toString(sleepPeriod));
                          }

                          reaperObject.wait(sleepPeriod);
                     }
                     catch (InterruptedException e1) {}

                    // test our condition -- things may have changed while we were waiting

                    if (_shutdown) {
                        return;
                    }
                }
            }
    
            if (tsLogger.logger.isTraceEnabled()) {
                tsLogger.logger.trace("ReaperThread.run ()");
            }

    	    reaperObject.check();
    	}
    }

    public void shutdown ()
    {
	_shutdown = true;
    }

    private TransactionReaper reaperObject;
    private long              sleepPeriod;
    private boolean           _shutdown;

    
}
