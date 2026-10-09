const API='/api';
const grid=document.getElementById('eventGrid'); let allEvents=[]; let me=null;
const money=n=>Number(n||0)===0?'Free':'₹'+Number(n).toLocaleString('en-IN');
const dateText=d=>d?new Date(d+'T00:00:00').toLocaleDateString('en-IN',{day:'numeric',month:'short',year:'numeric'}):'Date TBA';
const esc=s=>String(s??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
async function request(url,options={}){
 const res=await fetch(url,{headers:{'Content-Type':'application/json',...(options.headers||{})},credentials:'same-origin',...options});
 if(!res.ok){let msg='Request failed';try{const d=await res.json();msg=d.message||msg}catch{}throw new Error(msg)}
 if(res.status===204)return null; return res.json();
}
async function loadEvents(){try{allEvents=await request(API+'/events');renderEvents()}catch(e){grid.innerHTML='<div class="empty">Could not load events. Check Spring Boot and MySQL.</div>'}}
function renderEvents(){
 const q=document.getElementById('search').value.toLowerCase(),cat=document.getElementById('category').value.toLowerCase();
 const items=allEvents.filter(e=>(!cat||e.category.toLowerCase()===cat)&&[e.title,e.description,e.venue,e.category,e.organizerName].some(v=>(v||'').toLowerCase().includes(q)));
 if(!items.length){grid.innerHTML='<div class="empty">No events found.</div>';return}
 grid.innerHTML=items.map((e,i)=>`<article class="event-card"><div class="event-art"><span>${['✳','◈','✦','❋','✺'][i%5]}</span><small>${esc(e.category)}</small></div><div class="event-body"><span class="tag">${esc(e.status||'OPEN')}</span><h3>${esc(e.title)}</h3><p>${esc(e.description||'Join us for this event.')}</p><div class="event-meta">▦ ${dateText(e.eventDate)}${e.eventTime?' · '+esc(e.eventTime):''}<br>⌖ ${esc(e.venue)}<br>◉ ${money(e.registrationFee)} · ${e.capacity} seats<br>♙ ${esc(e.organizerName)}</div><button class="button" ${e.status!=='OPEN'?'disabled':''} onclick="openRegister(${e.id})">Register</button></div></article>`).join('')
}
document.getElementById('search').addEventListener('input',renderEvents);document.getElementById('category').addEventListener('change',renderEvents);

async function checkMe(){try{me=await request(API+'/auth/me')}catch{me=null}updateAuthUI();if(me)await loadDashboard()}
function updateAuthUI(){
 const logged=!!me, canManage=logged&&(me.role==='ADMIN'||me.role==='ORGANIZER');
 document.getElementById('loginBtn').classList.toggle('hidden',logged);
 document.getElementById('logoutBtn').classList.toggle('hidden',!logged);
 document.getElementById('organize').querySelector('.panel').classList.toggle('hidden',!canManage);
 document.getElementById('dashboard').classList.toggle('hidden',!logged);
 document.getElementById('registrationHint').textContent=logged&&me.role==='USER'?'Your registrations are private to your account.':'Login as a user to view your private registrations.';
 document.getElementById('lookupEmail').disabled=logged&&me.role==='USER';
 document.getElementById('lookupEmail').value=logged&&me.role==='USER'?me.email:'';
 document.getElementById('dashboardTitle').textContent=me?.role==='ADMIN'?'Admin Dashboard':me?.role==='ORGANIZER'?'Organizer Dashboard':'My Dashboard';
 document.getElementById('dashboardWelcome').textContent=logged?`Welcome, ${me.fullName} · ${me.role}`:'';
 document.getElementById('registrationPanel').classList.toggle('hidden',!canManage);
 document.getElementById('adminUsersPanel').classList.toggle('hidden',me?.role!=='ADMIN');
 document.getElementById('adminOrganizersPanel').classList.toggle('hidden',me?.role!=='ADMIN');
 document.getElementById('profilePanel').classList.toggle('hidden',!logged);
 document.getElementById('addEventBtn').classList.toggle('hidden',!canManage);
 document.getElementById('statPeople').parentElement.classList.toggle('hidden',me?.role==='USER');
 document.getElementById('statPeopleLabel').textContent=me?.role==='ADMIN'?'Users':'Attendees';
 if(logged)fillProfile();
}

document.getElementById('eventForm').addEventListener('submit',async ev=>{
 ev.preventDefault();const f=new FormData(ev.target),id=f.get('id'),data=Object.fromEntries(f.entries());
 data.capacity=Number(data.capacity);data.registrationFee=Number(data.registrationFee||0);
 
 try{await request(API+'/events'+(id?'/'+id:''),{method:id?'PUT':'POST',body:JSON.stringify(data)});showMsg('Event saved successfully.');resetEventForm();await loadEvents();await loadDashboard()}catch(e){showMsg(e.message)}
});
function showMsg(t){document.getElementById('eventMessage').textContent=t}
function resetEventForm(){const f=document.getElementById('eventForm');f.reset();f.querySelector('[name=id]').value='';document.getElementById('eventFormTitle').textContent='Create an event';document.getElementById('cancelEdit').classList.add('hidden');if(me?.role==='ORGANIZER')f.querySelector('[name=organizerName]').value=me.fullName}
document.getElementById('cancelEdit').onclick=resetEventForm;
document.getElementById('addEventBtn').onclick=()=>{resetEventForm();document.getElementById('eventForm').scrollIntoView({behavior:'smooth'})};

async function loadDashboard(){
 if(!me)return;
 try{
  if(me.role==='USER'){await loadUserDashboard();return}
  const events=await request(API+'/events/manage');
  document.getElementById('statEvents').textContent=events.length;
  document.getElementById('statOpen').textContent=events.filter(e=>String(e.status||'').toUpperCase()==='OPEN').length;
  let regs=[];for(const e of events){try{regs=regs.concat(await request(API+'/registrations/event/'+e.id))}catch{}}
  document.getElementById('statRegistrations').textContent=regs.length;
  document.getElementById('statPeople').textContent=me.role==='ADMIN'?await countUsers():new Set(regs.map(r=>r.userId)).size;
  renderManageEvents(events);renderManageRegistrations(regs);
  if(me.role==='ADMIN'){await loadAdminData();document.getElementById('profilePanel').classList.remove('hidden')}
 }catch(e){document.getElementById('manageEvents').innerHTML='<div class="empty">'+esc(e.message)+'</div>'}
}
async function loadUserDashboard(){
 const regs=await request(API+'/registrations/my');
 document.getElementById('statEvents').textContent=regs.length;
 document.getElementById('statRegistrations').textContent=regs.length;
 document.getElementById('statOpen').textContent=regs.filter(r=>r.event?.status==='OPEN').length;
 document.getElementById('statPeople').textContent='—';
 document.getElementById('statPeople').parentElement.classList.add('hidden');
 document.getElementById('manageEvents').innerHTML='<div class="empty">Users do not manage events. Browse the public event list above and register for events.</div>';
 document.getElementById('registrationPanel').classList.add('hidden');
 renderUserRegistrations(regs);
}
function renderManageEvents(events){
 if(!events.length){document.getElementById('manageEvents').innerHTML='<div class="empty">No events yet. Click Add Event.</div>';return}
 document.getElementById('manageEvents').innerHTML='<div class="table-wrap"><table><thead><tr><th>ID</th><th>Event</th><th>Date</th><th>Organizer</th><th>Status</th><th>Actions</th></tr></thead><tbody>'+events.map(e=>`<tr><td>${e.id}</td><td><strong>${esc(e.title)}</strong><small>${esc(e.category)}</small></td><td>${dateText(e.eventDate)}</td><td>${esc(e.organizerName)}</td><td><span class="status">${esc(e.status)}</span></td><td class="row-actions"><button onclick="editEvent(${e.id})">Edit</button><button class="danger" onclick="deleteEvent(${e.id})">Delete</button></td></tr>`).join('')+'</tbody></table></div>'
}
function renderManageRegistrations(regs){
 const panel=document.getElementById('manageRegistrations');if(!regs.length){panel.innerHTML='<div class="empty">No registrations yet.</div>';return}
 panel.innerHTML='<div class="table-wrap"><table><thead><tr><th>Event</th><th>User</th><th>Email</th><th>Phone</th><th>Status</th><th>Action</th></tr></thead><tbody>'+regs.map(r=>`<tr><td>${esc(r.event.title)}</td><td>${esc(r.fullName)}</td><td>${esc(r.email)}</td><td>${esc(r.phone||'-')}</td><td>${esc(r.status)}</td><td><button class="danger" onclick="deleteRegistration(${r.id})">Delete</button></td></tr>`).join('')+'</tbody></table></div>'
}
function renderUserRegistrations(regs){
 const target=document.getElementById('myRegistrations');
 if(!regs.length){target.innerHTML='<div class="empty">No registrations for your account yet.</div>';return}
 target.innerHTML='<div class="registration-list">'+regs.map(r=>`<div class="registration-item"><div><strong>${esc(r.event.title)}</strong><div class="muted">${dateText(r.event.eventDate)} · ${esc(r.event.venue)}</div><div class="muted">${esc(r.fullName)} · ${esc(r.status)}</div></div><button class="button secondary" onclick="cancelRegistration(${r.id})">Cancel</button></div>`).join('')+'</div>'
}
window.editEvent=id=>{
 const e=allEvents.find(x=>x.id===id);if(!e)return;const f=document.getElementById('eventForm');
 Object.entries(e).forEach(([k,v])=>{const el=f.querySelector(`[name="${k}"]`);if(el)el.value=v??''});
 f.querySelector('[name=id]').value=e.id;document.getElementById('eventFormTitle').textContent='Update event';document.getElementById('cancelEdit').classList.remove('hidden');document.getElementById('organize').scrollIntoView({behavior:'smooth'})
};
window.deleteEvent=async id=>{if(!confirm('Delete this event?'))return;try{await request(API+'/events/'+id,{method:'DELETE'});await loadEvents();await loadDashboard()}catch(e){alert(e.message)}};
window.deleteRegistration=async id=>{if(!confirm('Delete this registration?'))return;try{await request(API+'/registrations/'+id,{method:'DELETE'});await loadDashboard()}catch(e){alert(e.message)}};
window.cancelRegistration=async id=>{if(!confirm('Cancel this registration?'))return;try{await request(API+'/registrations/'+id,{method:'DELETE'});await loadDashboard();document.getElementById('registrations').scrollIntoView({behavior:'smooth'})}catch(e){alert(e.message)}};

const modal=document.getElementById('modal');document.getElementById('closeModal').onclick=()=>modal.classList.add('hidden');modal.addEventListener('click',e=>{if(e.target===modal)modal.classList.add('hidden')});
window.openRegister=id=>{
 const e=allEvents.find(x=>x.id===id);if(!e)return;
 if(!me||me.role!=='USER'){openLogin('Please login with a USER account to register for events.');return}
 document.getElementById('modalContent').innerHTML=`<span class="eyebrow">EVENT REGISTRATION</span><h2>${esc(e.title)}</h2><p class="muted">${dateText(e.eventDate)} · ${esc(e.venue)} · ${money(e.registrationFee)}</p><form id="registerForm" class="modal-form"><label>Full name<input name="fullName" value="${esc(me.fullName)}" disabled></label><label>Email address<input name="email" value="${esc(me.email)}" disabled></label><label>Phone<input name="phone" type="tel" maxlength="30"></label><button class="button">Confirm registration</button><p id="registerMessage" class="message"></p></form>`;
 modal.classList.remove('hidden');
 document.getElementById('registerForm').onsubmit=async ev=>{ev.preventDefault();const data=Object.fromEntries(new FormData(ev.target).entries());data.eventId=String(id);try{await request(API+'/registrations',{method:'POST',body:JSON.stringify(data)});document.getElementById('registerMessage').textContent='Registration confirmed!';ev.target.querySelector('button').disabled=true;await loadDashboard()}catch(e){document.getElementById('registerMessage').textContent=e.message}}
};

document.getElementById('lookupForm').addEventListener('submit',async ev=>{
 ev.preventDefault();const target=document.getElementById('myRegistrations');
 if(me?.role==='USER'){await loadUserDashboard();return}
 if(me?.role!=='ADMIN'){target.innerHTML='<div class="empty">Please login as a USER to view private registrations.</div>';return}
 const email=document.getElementById('lookupEmail').value.trim();target.innerHTML='<p class="muted">Searching…</p>';
 try{const regs=await request(API+'/registrations/email/'+encodeURIComponent(email));renderUserRegistrations(regs)}catch(e){target.innerHTML='<div class="empty">'+esc(e.message)+'</div>'}
});

function openLogin(message=''){
 document.getElementById('modalContent').innerHTML=`<span class="eyebrow">ACCOUNT ACCESS</span><h2>Login</h2><p class="muted">${esc(message||'Choose your role account to continue.')}</p><form id="loginForm" class="modal-form"><label>Username<input name="username" required></label><label>Password<input name="password" type="password" required></label><button class="button">Login</button><p class="message" id="loginMessage"></p></form><div class="demo-box"><strong>Demo accounts</strong><br>Admin: admin / admin123<br>Organizer: organizer1 / organizer123<br>User: user1 / user123</div><button class="button secondary" id="showRegisterBtn">Create User Account</button>`;
 modal.classList.remove('hidden');
 document.getElementById('loginForm').onsubmit=async ev=>{ev.preventDefault();try{me=await request(API+'/auth/login',{method:'POST',body:JSON.stringify(Object.fromEntries(new FormData(ev.target).entries()))});modal.classList.add('hidden');updateAuthUI();await loadEvents();await loadDashboard();resetEventForm()}catch(e){document.getElementById('loginMessage').textContent=e.message}};
 document.getElementById('showRegisterBtn').onclick=openRegisterAccount;
}
function openRegisterAccount(){
 document.getElementById('modalContent').innerHTML=`<span class="eyebrow">NEW ACCOUNT</span><h2>Create User Account</h2><p class="muted">Public registration creates a USER account only. Admin and Organizer accounts are managed separately.</p><form id="accountForm" class="modal-form"><label>Full name<input name="fullName" required></label><label>Username<input name="username" required></label><label>Email<input name="email" type="email" required></label><label>Password<input name="password" type="password" minlength="6" required></label><button class="button">Create Account</button><p id="accountMessage" class="message"></p></form><button class="button secondary" id="backLogin">Back to Login</button>`;
 document.getElementById('accountForm').onsubmit=async ev=>{ev.preventDefault();try{await request(API+'/auth/register',{method:'POST',body:JSON.stringify(Object.fromEntries(new FormData(ev.target).entries()))});document.getElementById('accountMessage').textContent='Account created. You can now login.';ev.target.reset()}catch(e){document.getElementById('accountMessage').textContent=e.message}};
 document.getElementById('backLogin').onclick=()=>openLogin();
}
document.getElementById('loginBtn').onclick=()=>openLogin();
document.getElementById('organizerLoginBtn').onclick=()=>openLogin();
document.getElementById('logoutBtn').onclick=async()=>{try{await request(API+'/auth/logout',{method:'POST'})}finally{me=null;document.getElementById('dashboard').classList.add('hidden');updateAuthUI();resetEventForm();await loadEvents()}};

async function countUsers(){try{const u=await request(API+'/admin/users');return u.length}catch{return 0}}
async function loadAdminData(){
 const [users,orgs]=await Promise.all([request(API+'/admin/users'),request(API+'/admin/organizers')]);
 document.getElementById('adminUsers').innerHTML=users.length?`<div class="table-wrap"><table><thead><tr><th>User</th><th>Email</th><th>Status</th><th>Action</th></tr></thead><tbody>${users.map(u=>`<tr><td><strong>${esc(u.fullName)}</strong><small>${esc(u.username)}</small></td><td>${esc(u.email)}</td><td>${esc(u.status)}</td><td><button onclick="toggleAccount(${u.id},'${u.status}')">${u.status==='ACTIVE'?'Deactivate':'Activate'}</button><button class="danger" onclick="deleteAccount(${u.id},'user')">Delete</button></td></tr>`).join('')}</tbody></table></div>`:'<div class="empty">No users.</div>';
 document.getElementById('adminOrganizers').innerHTML=orgs.length?`<div class="table-wrap"><table><thead><tr><th>Organizer</th><th>Email</th><th>Events</th><th>Status</th><th>Action</th></tr></thead><tbody>${orgs.map(o=>`<tr><td><strong>${esc(o.fullName)}</strong><small>${esc(o.username)}</small></td><td>${esc(o.email)}</td><td>${allEvents.filter(e=>e.organizerId===o.id).length}</td><td>${esc(o.status)}</td><td><button onclick="toggleAccount(${o.id},'${o.status}',true)">${o.status==='ACTIVE'?'Deactivate':'Activate'}</button><button class="danger" onclick="deleteAccount(${o.id},'organizer')">Delete</button></td></tr>`).join('')}</tbody></table></div>`:'<div class="empty">No organizers.</div>';
}
window.toggleAccount=async(id,status,organizer=false)=>{try{await request(`${API}/admin/${organizer?'organizers':'users'}/${id}/status`,{method:'PUT',body:JSON.stringify({status:status==='ACTIVE'?'INACTIVE':'ACTIVE'})});await loadDashboard()}catch(e){alert(e.message)}};
window.deleteAccount=async(id,type)=>{if(!confirm(`Delete this ${type}? This action cannot be undone.`))return;try{await request(`${API}/admin/${type==='organizer'?'organizers':'users'}/${id}`,{method:'DELETE'});await loadEvents();await loadDashboard()}catch(e){alert(e.message)}};
document.getElementById('addOrganizerBtn').onclick=()=>{
 document.getElementById('modalContent').innerHTML=`<span class="eyebrow">ADMIN</span><h2>Create Organizer</h2><form id="orgForm" class="modal-form"><label>Full name<input name="fullName" required></label><label>Username<input name="username" required></label><label>Email<input name="email" type="email" required></label><label>Password<input name="password" type="password" minlength="6" required></label><button class="button">Create Organizer</button><p id="orgMessage" class="message"></p></form>`;
 modal.classList.remove('hidden');
 document.getElementById('orgForm').onsubmit=async ev=>{ev.preventDefault();try{await request(API+'/admin/organizers',{method:'POST',body:JSON.stringify(Object.fromEntries(new FormData(ev.target).entries()))});document.getElementById('orgMessage').textContent='Organizer created successfully.';ev.target.reset();await loadDashboard()}catch(e){document.getElementById('orgMessage').textContent=e.message}}
};

function fillProfile(){const f=document.getElementById('profileForm');f.fullName.value=me.fullName||'';f.email.value=me.email||''}
document.getElementById('profileForm').onsubmit=async ev=>{ev.preventDefault();try{me=await request(API+'/auth/profile',{method:'PUT',body:JSON.stringify(Object.fromEntries(new FormData(ev.target).entries()))});document.getElementById('profileMessage').textContent='Profile updated successfully.';updateAuthUI()}catch(e){document.getElementById('profileMessage').textContent=e.message}};
document.getElementById('refreshDashboard').onclick=loadDashboard;

checkMe();loadEvents();
