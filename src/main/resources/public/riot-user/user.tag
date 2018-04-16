<user>
  <h3>{ opts.title }</h3>

  <div if={ state.isLoaded }>

      <table class="ui celled padded table">
        <thead>
          <tr>
            <th class="single line">Active</th>
            <th>Name</th>
            <th>EMail</th>
            <th>action</th>
          </tr>
        </thead>
        <tbody>
          <tr each={ state.users.filter(whatShow) }>
            <td>
              <label class={ completed: !active }>
                <input type="checkbox" checked={ active } onclick={ parent.toggle } />
              </label>
            </td>
            <td class="single line">
              <div class={ completed: !active }>{ name }</div>
            </td>
            <td class="single line">
              <div class={ completed: !active }>{ email }</div>
            </td>
            <td class="single line">
              <i class="edit icon disabled"></i>&nbsp;<i class="trash alternate icon" onclick={ delete }></i>
            </td>
          </tr>
        </tbody>
      </table>

      <form class="ui form" onsubmit={ add }>
        <div class="field">
          <label>Name</label>
          <input type="text" name="name" placeholder="Name" ref="name" onkeyup={ validate }/>
        </div>
        <div class="field">
          <label>EMail</label>
          <input type="text" name="email" placeholder="email" ref="email" onkeyup={ validate }/>
        </div>
        <div class="field">
          <div class="ui checkbox">
            <input type="checkbox" tabindex="0" ref="accepted" onclick={ validate }/>
            <label>I agree to the Terms and Conditions</label>
          </div>
        </div>
        <button class="ui button" disabled={ !valid }>Add #{ state.users.filter(whatShow).length + 1 }</button>

        <button class="ui button" type="button" disabled={ state.users.filter(inActive).length == 0 } onclick={ disableUsers }>
          Delete In-Active { state.users.filter(inActive).length }
        </button>
      </form>

  </div>

  <div if={ !state.isLoaded }>
    <h1>Loading...</h1>
  </div>

  <!-- this script tag is optional -->
  <script>
    var self = this
    self.state = { isLoaded: false, users: [] }

    self.on('mount', function() {
      // Trigger init event when component is mounted to page.
      // Any store could respond to this.
      RiotControl.trigger('users_init')
    })

    // Register a listener for store change events.
    RiotControl.on('users_changed', function(usersState) {
      console.log('users changed, updating user tag')
      self.state = usersState
      self.update()
    })

    validate() {
      console.log(this.refs.email.value)
      console.log(this.refs.name.value)
      console.log(this.refs.accepted.checked)
      self.valid = this.refs.email.value.length > 0 && this.refs.name.value.length > 0 && this.refs.accepted.checked
      console.log(`valid=${self.valid}`);
      return self.valid
    }

    add(e) {
      e.preventDefault()
      if (this.validate()) {
        console.log('processing new user submission')
        var newUser = { email: this.refs.email.value, name: this.refs.name.value, active: this.refs.accepted.checked }
        RiotControl.trigger('users_add', newUser)
        this.refs.email.value = this.refs.name.value = ''
        this.refs.accepted.value = false
      } else {
        console.log('form not valid')
      }
    }

    delete(e) {
      e.preventDefault()
      console.log('Deleting '+e.item.id)
      RiotControl.trigger('users_remove', [e.item])
    }

    disableUsers(e) {
      RiotControl.trigger('users_remove', self.state.users.filter(function(user) {
        return !user.active
      }))
    }

    // an two example how to filter items on the list
    whatShow(user) {
      return !user.hidden
    }

    inActive(user) {
      return !user.active
    }

    toggle(e) {
      var user = e.item
      user.active = !user.active
      return true
    }

  </script>

</user>
