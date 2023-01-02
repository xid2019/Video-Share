import { NgModule } from '@angular/core';
import { AuthModule } from 'angular-auth-oidc-client';


@NgModule({
    imports: [AuthModule.forRoot({
        config: {
            authority: 'https://dev-8vaeylny3lsnei0g.us.auth0.com',
            redirectUrl: window.location.origin,
            clientId: 'R8avCmEg8JCKeFS7r3AHCe8OymH3du7R',
            scope: 'openid profile offline_access email',
            responseType: 'code',
            silentRenew: true,
            useRefreshToken: true,
            secureRoutes:['http://localhost:8080/'],
            customParamsAuthRequest: {
                audience: 'http://localhost:8080/'
            }
        }
      })],
    exports: [AuthModule],
})
export class AuthConfigModule {}
