import React from 'react';

import FileUploadModalBox from './FileUploadModalBox';

import Dropdown from 'react-bootstrap/Dropdown';
import Nav from 'react-bootstrap/Nav';
import Navbar from 'react-bootstrap/Navbar';

class TopNavbar extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      user: null,
      modalShow: false,
    };
    this.handleModalClose = this.handleModalClose.bind(this);
  }
  
  handleModalClose() {
    this.setState({modalShow: false });
  }

  componentDidMount() {
    this.login();
  }
  
  login() {
    fetch('/login')
      .then(response => response.json())
      .then(userInfo => {
        sessionStorage.setItem('logged-in', userInfo.email);
        this.setState({user: userInfo});
      })
      .catch(() => {
        sessionStorage.setItem('logged-in', '');
        this.setState({user: null});
    });
  }

  render() {
    return (
      <Navbar bg='dark' variant='dark' fixed='top' expand='lg'>
        <Navbar.Brand href='/'>EDITH</Navbar.Brand>
          <Nav className='ml-auto links'>
            <Nav.Link href='#home' className='home'>HOME</Nav.Link>
            <Nav.Link href='#features' className='features'>FEATURES</Nav.Link>
            {this.state.user === null &&
              <Nav.Link href='/login' className='login-button'>LOG IN</Nav.Link>
            }
            {this.state.user &&
              <>
                <Nav.Link href='#dashboard' className='dashboard'>DASHBOARD</Nav.Link>
                <Dropdown className='dropdowns'>
                  <Dropdown.Toggle variant='dark' id='dropdown-basic' className='dropdown-toggle'>
                    {this.state.user.email}
                  </Dropdown.Toggle>
                  <Dropdown.Menu className='dropdown-menu'>
                    <Dropdown.Item onClick={() => this.setState({modalShow: true})} className='upload-receipt'>Upload Receipt</Dropdown.Item>
                    <FileUploadModalBox
                      show={this.state.modalShow}
                      handleModalClose={this.handleModalClose}
                    />
                    <Dropdown.Item className='set-nickname'>Set Nickname</Dropdown.Item>
                    <Dropdown.Item className='invite-friends'>Invite Friends</Dropdown.Item>
                    <Dropdown.Item href={this.state.user.logOutUrl} className='log-out'>Log Out</Dropdown.Item>
                  </Dropdown.Menu>
                </Dropdown>
              </>
            }
          </Nav>
      </Navbar>
    );
  }
}

export default TopNavbar;
