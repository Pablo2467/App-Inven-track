import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { API_URL } from './config';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  const loginResponse = {
    token: 'abc123',
    tokenType: 'Bearer',
    expiresInSeconds: 28800,
    user: { id: 1, fullName: 'Dueño', username: 'dueno', role: 'DUENO' },
  };

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  it('guarda el token y el usuario al iniciar sesión', () => {
    service.login('dueno', 'Dueno123*').subscribe();

    const req = http.expectOne(`${API_URL}/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush(loginResponse);

    expect(service.token()).toBe('abc123');
    expect(service.isLoggedIn()).toBe(true);
    expect(service.user()?.role).toBe('DUENO');
  });

  it('borra la sesión al cerrar sesión', () => {
    service.login('dueno', 'Dueno123*').subscribe();
    http.expectOne(`${API_URL}/auth/login`).flush(loginResponse);

    service.logout();

    expect(service.token()).toBeNull();
    expect(service.isLoggedIn()).toBe(false);
  });
});