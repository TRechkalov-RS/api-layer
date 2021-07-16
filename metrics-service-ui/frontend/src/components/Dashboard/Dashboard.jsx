/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright Contributors to the Zowe Project.
 */

import Typography from '@material-ui/core/Typography';
import Container from '@material-ui/core/Container';
import React, { useEffect } from 'react';

export default function Dashboard() {
    useEffect(() => {
        setTimeout(() => {
            window.addStreams('https://localhost:10012/discoverableclient/application/hystrix.stream');
        }, 0);
    }, []);
    return (
        <React.Fragment>
            <Typography id="name" variant="h2" component="h1" gutterBottom align="center">
                Metrics Service
            </Typography>
            <Container maxWidth="false" id="content" className="dependencies" />
        </React.Fragment>
    );
}
