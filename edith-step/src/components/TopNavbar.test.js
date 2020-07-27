import React from 'react';
import {enableFetchMocks} from 'jest-fetch-mock';
import {mount} from 'enzyme';
import TopNavbar from './TopNavbar';

import '../setupTests.js';

let component;
let handleModalClose;
const userObj = {'email': 'email'};

describe('TopNavbar calls', () => {
  test('login method when mounted', () => {
    const login = jest.spyOn(TopNavbar.prototype, 'login');
    component = mount(<TopNavbar />);
    expect(login).toBeCalled();
  });
});

describe('When not logged in, Top Navigation Bar', () => {
  beforeEach(() => {
    enableFetchMocks();
    component = mount(<TopNavbar />);
    component.setState({user: null});
  });

  afterEach(() => {
    component.unmount();
  });

  // Checks if the Navigation Bar is rendered.
  test('renders', () => {
    expect(component.exists()).toBe(true);
  });

  // Checks if the navigation Bar provides three different options.
  test('has three options', () => {
    expect(component.find('.links').length).toBe(3);
  });

  // Checks if there is Home option.
  test('has Home option', () => {
    expect(component.find('.home').exists()).toBe(true);
  });

  // Checks if there is Features option.
  test('has Features option', () => {
    expect(component.find('.features').exists()).toBe(true);
  });

  // Checks if there is Login option.
  test('has Login option', () => {
    expect(component.find('.login-button').exists()).toBe(true);
  });

  // Checks if there is Dashboard option.
  test('has Dashboard section', () => {
    expect(component.find('.dashboard').exists()).toBe(false);
  });

  // Checks if dropdown is shown.
  test('should not display dropdown', () => {
    expect(component.find('.dropdowns').exists()).toBe(false);
  });
});

describe('When logged in, Top Navigation Bar', () => {
  beforeEach(() => {
    enableFetchMocks();
    component = mount(<TopNavbar />);
    handleModalClose = jest.spyOn(TopNavbar.prototype, 'handleModalClose');
    component.setState({user: userObj});
  });

  afterEach(() => {
    component.unmount();
  });

  // Checks if the Navigation Bar is rendered.
  test('renders', () => {
    expect(component.exists()).toBe(true);
  });

  // Checks if there is Dashboard option.
  test('has Dashboard section', () => {
    expect(component.find('.dashboard').exists()).toBe(true);
  });

  // Checks if there is Login option.
  test('does not have Login option', () => {
    expect(component.find('.login-button').exists()).toBe(false);
  });

  // Checks if there is Dropdown menu.
  test('has Dropdown menu', () => {
    expect(component.find('.dropdowns').exists()).toBe(true);
  });

  // Checks if upload modal box opens when button is clicked from dropdown.
  test('opens upload modal box when upload receipt button is clicked', () => {
    expect(component.state('modalShow')).toBe(false);
    component.find('.dropdown-toggle').at(0).simulate('click');
    component.find('.upload-receipt').at(0).simulate('click');
    expect(component.state('modalShow')).toBe(true);
  });

  // Checks if the modal box closes when close button is clicked.
  test('closes upload modal box when close button is clicked', () => {
    component.find('.dropdown-toggle').at(0).simulate('click');
    component.find('.upload-receipt').at(0).simulate('click');
    component.find('.close').at(0).simulate('click');
    expect(handleModalClose).toBeCalled();
  });
});
